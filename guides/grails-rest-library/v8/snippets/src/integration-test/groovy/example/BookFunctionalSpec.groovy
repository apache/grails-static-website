package example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Functional tests drive the real REST/JSON surface over HTTP with
 * java.net.http.HttpClient. @Rollback is deliberately NOT used: the
 * endpoints commit, so the fixture is seeded (and read back) inside
 * withNewTransaction blocks. BootStrap data is not populated in the
 * @Integration context, so each spec owns its fixture.
 */
@Integration
class BookFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    HttpClient client = HttpClient.newHttpClient()

    Long authorId

    void setup() {
        Book.withNewTransaction {
            Author a = Author.findByName('Functional Author') ?: new Author(name: 'Functional Author').save(failOnError: true)
            authorId = a.id
            if (!Book.findByIsbn('9781111111113')) {
                new Book(author: a, title: 'Seeded One', isbn: '9781111111113', pageCount: 100).save(failOnError: true)
            }
            if (!Book.findByIsbn('9782222222226')) {
                new Book(author: a, title: 'Seeded Two', isbn: '9782222222226', pageCount: 200).save(failOnError: true)
            }
        }
    }

    private URI uri(String path) { URI.create("http://localhost:${serverPort}${path}") }

    private HttpResponse<String> getJson(String path) {
        client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> postJson(String path, String body) {
        client.send(HttpRequest.newBuilder(uri(path))
                .header('Content-Type', 'application/json')
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())
    }

    void "GET /v1/books returns a paginated JSON envelope"() {
        when:
        HttpResponse<String> resp = getJson('/v1/books')
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.containsKey('page')
        json.containsKey('pageSize')
        json.total >= 2
        json.items.size() >= 2
        json.items.every { it.title && it.isbn && it.author?.name }
    }

    void "GET /v1/books?max= clamps the page size to 100"() {
        when:
        HttpResponse<String> resp = getJson('/v1/books?max=500')
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.pageSize == 100
    }

    void "POST /v1/books/bulk creates every book and returns 201"() {
        given:
        String body = JsonOutput.toJson([
            [title: 'The Two Towers',         isbn: '9780547928203', pageCount: 416, author: [id: authorId]],
            [title: 'The Return of the King', isbn: '9780547928197', pageCount: 432, author: [id: authorId]]
        ])

        when:
        HttpResponse<String> resp = postJson('/v1/books/bulk', body)

        then:
        resp.statusCode() == 201
        Book.withNewTransaction {
            Book.findByIsbn('9780547928203') != null && Book.findByIsbn('9780547928197') != null
        }
    }

    void "POST /v1/books/bulk rolls back entirely when any entry is invalid"() {
        given: 'one valid and one invalid entry in the same request'
        long before = Book.withNewTransaction { Book.count() }
        String body = JsonOutput.toJson([
            [title: 'Would Be Valid', isbn: '9780000000086', pageCount: 100, author: [id: authorId]],
            [title: '',               isbn: 'not-an-isbn',   author: [id: authorId]]
        ])

        when:
        HttpResponse<String> resp = postJson('/v1/books/bulk', body)
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then: 'a 422 reports the failing index and NOTHING is persisted'
        resp.statusCode() == 422
        json.books.size() == 1
        json.books.first().index == 1
        Book.withNewTransaction { Book.count() } == before
        Book.withNewTransaction { Book.findByIsbn('9780000000086') == null }
    }

    void "GET /v1/authors returns the authors as JSON"() {
        when:
        HttpResponse<String> resp = getJson('/v1/authors')
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.total >= 1
        json.items.any { it.name == 'Functional Author' }
    }

    // -------------------------------------------------------------------------
    // Show (GET /v1/books/{id})
    // -------------------------------------------------------------------------

    void "GET /v1/books/{id} returns a single book"() {
        given:
        Long bookId = Book.withNewTransaction {
            Book.findByIsbn('9781111111113').id
        }

        when:
        HttpResponse<String> resp = getJson("/v1/books/${bookId}")
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.title == 'Seeded One'
        json.isbn == '9781111111113'
        json.pageCount == 100
        json.author.name == 'Functional Author'
        json._links.self.href.contains("id=${bookId}")
    }

    void "GET /v1/books/{id} returns 404 for non-existent book"() {
        when:
        HttpResponse<String> resp = getJson('/v1/books/999999')

        then:
        resp.statusCode() == 404
    }

    // -------------------------------------------------------------------------
    // Save (POST /v1/books)
    // -------------------------------------------------------------------------

    void "POST /v1/books creates a new book"() {
        given:
        String body = JsonOutput.toJson([
            title: 'Functional Created', isbn: '9780000000003', pageCount: 250,
            author: [id: authorId]
        ])

        when:
        HttpResponse<String> resp = postJson('/v1/books', body)
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 201
        json.title == 'Functional Created'
        json.isbn == '9780000000003'

        cleanup:
        Book.withNewTransaction { Book.findByIsbn('9780000000003')?.delete(flush: true) }
    }

    void "POST /v1/books returns 422 when validation fails"() {
        given:
        String body = JsonOutput.toJson([
            title: '', isbn: 'bad-isbn', author: [id: authorId]
        ])

        when:
        HttpResponse<String> resp = postJson('/v1/books', body)
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 422
        json.errors.any { it.field == 'title' }
        json.errors.any { it.field == 'isbn' }
    }

    // -------------------------------------------------------------------------
    // Update (PUT /v1/books/{id})
    // -------------------------------------------------------------------------

    private HttpResponse<String> putJson(String path, String body) {
        client.send(HttpRequest.newBuilder(uri(path))
                .header('Content-Type', 'application/json')
                .PUT(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())
    }

    void "PUT /v1/books/{id} updates an existing book"() {
        given:
        Long bookId = Book.withNewTransaction { Book.findByIsbn('9782222222226').id }
        String body = JsonOutput.toJson([title: 'Updated Title', pageCount: 999])

        when:
        HttpResponse<String> resp = putJson("/v1/books/${bookId}", body)
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.title == 'Updated Title'
        json.pageCount == 999

        cleanup:
        Book.withNewTransaction {
            Book b = Book.findByIsbn('9782222222226')
            if (b) {
                b.title = 'Seeded Two'
                b.pageCount = 200
                b.save(flush: true, failOnError: true)
            }
        }
    }

    void "PUT /v1/books/{id} returns 422 when validation fails"() {
        given:
        Long bookId = Book.withNewTransaction { Book.findByIsbn('9781111111113').id }
        String body = JsonOutput.toJson([title: ''])

        when:
        HttpResponse<String> resp = putJson("/v1/books/${bookId}", body)

        then:
        resp.statusCode() == 422
    }

    void "PUT /v1/books/{id} returns 404 for non-existent book"() {
        when:
        HttpResponse<String> resp = putJson('/v1/books/999999', JsonOutput.toJson([title: 'Nope']))

        then:
        resp.statusCode() == 404
    }

    // -------------------------------------------------------------------------
    // Delete (DELETE /v1/books/{id})
    // -------------------------------------------------------------------------

    private HttpResponse<String> deleteResource(String path) {
        client.send(HttpRequest.newBuilder(uri(path)).DELETE().build(),
                HttpResponse.BodyHandlers.ofString())
    }

    void "DELETE /v1/books/{id} deletes a book"() {
        given: 'a disposable book not used by other tests'
        Long disposableId = Book.withNewTransaction {
            new Book(author: Author.findByName('Functional Author'),
                     title: 'Delete Me Book', isbn: '9789999999997', pageCount: 1)
                .save(flush: true, failOnError: true).id
        }

        when:
        HttpResponse<String> resp = deleteResource("/v1/books/${disposableId}")

        then:
        resp.statusCode() == 204
        Book.withNewTransaction { Book.get(disposableId) == null }
    }

    void "DELETE /v1/books/{id} returns 404 for non-existent book"() {
        when:
        HttpResponse<String> resp = deleteResource('/v1/books/999999')

        then:
        resp.statusCode() == 404
    }

    // -------------------------------------------------------------------------
    // Author filter on GET /v1/books
    // -------------------------------------------------------------------------

    void "GET /v1/books?author= filters by author"() {
        given: 'two authors, each owning a distinct book, so the filter has something to exclude'
        Long targetAuthorId = Book.withNewTransaction {
            Author target = new Author(name: 'Filter Target Author').save(failOnError: true)
            Author other = new Author(name: 'Filter Other Author').save(failOnError: true)
            new Book(author: target, title: 'Target Book', isbn: '9784444444443', pageCount: 100).save(failOnError: true)
            new Book(author: other, title: 'Excluded Book', isbn: '9785555555556', pageCount: 100).save(failOnError: true)
            target.id
        }

        when:
        HttpResponse<String> resp = getJson("/v1/books?author=${targetAuthorId}")
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then: 'only the target author\'s books come back; the other author\'s book is excluded'
        resp.statusCode() == 200
        json.items.size() > 0
        json.items.every { it.author.id == targetAuthorId }
        json.items.every { it.isbn != '9785555555556' }
    }

    // -------------------------------------------------------------------------
    // Negative offset clamping
    // -------------------------------------------------------------------------

    void "GET /v1/books?offset= clamps negative offset to 0"() {
        when:
        HttpResponse<String> resp = getJson('/v1/books?offset=-5')
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.page == 0
    }
}

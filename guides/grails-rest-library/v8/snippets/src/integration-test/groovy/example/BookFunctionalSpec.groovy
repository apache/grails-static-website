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
}

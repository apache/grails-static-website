package admin

import example.Book
import grails.testing.mixin.integration.Integration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Functional test for the admin webapp's REST CRUD over real HTTP. The
 * Book domain and BookService it persists through both come from the
 * shared-core plugin - this proves the cross-module reuse works end to end
 * in a booted app. @Rollback is not used (the endpoint commits), so the
 * fixture is read back inside withNewTransaction.
 */
@Integration
class AdminBookFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    HttpClient client = HttpClient.newHttpClient()

    private URI uri(String path) { URI.create("http://localhost:${serverPort}${path}") }

    void "POST /books creates a shared-core Book and returns 201"() {
        given:
        String body = JsonOutput.toJson([title: 'Dune', isbn: '9780441013593', pageCount: 412])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/books'))
                        .header('Content-Type', 'application/json')
                        .header('Accept', 'application/json')
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 201
        Book.withNewTransaction { Book.findByIsbn('9780441013593')?.title == 'Dune' }
    }

    void "GET /books lists the shared-core Books as JSON"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780547928227')) {
                new Book(title: 'The Hobbit', isbn: '9780547928227', pageCount: 310).save(failOnError: true)
            }
        }

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/books.json')).GET().build(),
                HttpResponse.BodyHandlers.ofString())
        def json = new JsonSlurper().parseText(resp.body())

        then:
        resp.statusCode() == 200
        json*.isbn.contains('9780547928227')
    }

    void "POST /books with a malformed isbn returns 422"() {
        given:
        String body = JsonOutput.toJson([title: 'Bad', isbn: 'not-an-isbn'])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/books'))
                        .header('Content-Type', 'application/json')
                        .header('Accept', 'application/json')
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 422
    }

    void "GET /books/:id shows a single book as JSON"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780451524935')) {
                new Book(title: '1984', isbn: '9780451524935', pageCount: 328).save(failOnError: true)
            }
        }
        // Retrieve the id that was assigned
        Long bookId = Book.withNewTransaction { Book.findByIsbn('9780451524935').id }

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri("/books/${bookId}.json")).GET().build(),
                HttpResponse.BodyHandlers.ofString())
        def json = new JsonSlurper().parseText(resp.body())

        then:
        resp.statusCode() == 200
        json.isbn == '9780451524935'
        json.title == '1984'
    }

    void "PUT /books/:id updates an existing book"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780141439518')) {
                new Book(title: 'Pride', isbn: '9780141439518', pageCount: 280).save(failOnError: true)
            }
        }
        Long bookId = Book.withNewTransaction { Book.findByIsbn('9780141439518').id }
        String updateBody = JsonOutput.toJson([title: 'Pride and Prejudice', isbn: '9780141439518', pageCount: 432])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri("/books/${bookId}"))
                        .header('Content-Type', 'application/json')
                        .header('Accept', 'application/json')
                        .PUT(HttpRequest.BodyPublishers.ofString(updateBody)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 200
        Book.withNewTransaction {
            Book.findByIsbn('9780141439518').title == 'Pride and Prejudice'
        }
    }

    void "DELETE /books/:id removes a book"() {
        given:
        Long bookId = Book.withNewTransaction {
            def b = new Book(title: 'Temp', isbn: '9780000000999', pageCount: 50).save(failOnError: true)
            b.id
        }

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri("/books/${bookId}"))
                        .header('Accept', 'application/json')
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 204
        Book.withNewTransaction { Book.get(bookId) == null }
    }

    void "GET /books/:id returns 404 for a nonexistent id"() {
        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/books/99999.json')).GET().build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 404
    }
}

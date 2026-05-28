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

    void "POST /book creates a shared-core Book and returns 201"() {
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

    void "GET /book lists the shared-core Books as JSON"() {
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

    void "POST /book with a malformed isbn returns 422"() {
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
}

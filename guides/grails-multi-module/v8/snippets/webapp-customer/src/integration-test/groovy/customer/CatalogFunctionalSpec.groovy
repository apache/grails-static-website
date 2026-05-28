package customer

import example.Book
import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Functional test for the customer webapp's read-only catalog over real
 * HTTP. The CatalogController injects the shared-core BookService and the
 * Book domain, proving the same shared module backs the customer app too.
 */
@Integration
class CatalogFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    HttpClient client = HttpClient.newHttpClient()

    void setup() {
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780547928227')) {
                new Book(title: 'The Hobbit', isbn: '9780547928227', pageCount: 310).save(failOnError: true)
            }
            if (!Book.findByIsbn('9780441013593')) {
                new Book(title: 'Dune', isbn: '9780441013593', pageCount: 412).save(failOnError: true)
            }
        }
    }

    void "GET /catalog lists the shared-core Books as JSON"() {
        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:${serverPort}/catalog.json"))
                        .header('Accept', 'application/json')
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString())
        def json = new JsonSlurper().parseText(resp.body())

        then:
        resp.statusCode() == 200
        json*.isbn.contains('9780547928227')
        json*.isbn.contains('9780441013593')
    }
}

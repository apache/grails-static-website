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
 * Functional tests over real HTTP with java.net.http.HttpClient.
 *
 * Covers the JSON API the SPA consumes, the same-origin layout's CORS
 * behaviour (there is none - and that is the point of the guide), and the
 * SPA-serving path. The SPA test also exercises the Vite build + copy
 * integration: integrationTest's processResources runs frontendBuild and
 * copies dist/ into public/, so GET / serves the built index.html.
 *
 * @Rollback is not used (the endpoints commit); the fixture is seeded and
 * read back inside withNewTransaction blocks with ISBNs unique per test.
 */
@Integration
class ApiFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    HttpClient client = HttpClient.newHttpClient()

    private URI uri(String path) { URI.create("http://localhost:${serverPort}${path}") }

    void "GET /api/books returns the paginated JSON envelope"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780441013593')) {
                new Book(title: 'Dune', author: 'Frank Herbert', isbn: '9780441013593').save(failOnError: true)
            }
        }

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books')).GET().build(),
                HttpResponse.BodyHandlers.ofString())
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        resp.headers().firstValue('Content-Type').get().contains('application/json')
        json.containsKey('total')
        json.items.any { it.isbn == '9780441013593' }
    }

    void "POST /api/books creates a book and returns 201"() {
        given:
        String body = JsonOutput.toJson([title: 'Neuromancer', author: 'William Gibson', isbn: '9780441569595'])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books'))
                        .header('Content-Type', 'application/json')
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 201
        Book.withNewTransaction { Book.findByIsbn('9780441569595') != null }
    }

    void "POST /api/books with a malformed isbn returns 422"() {
        given:
        String body = JsonOutput.toJson([title: 'Bad', author: 'Nobody', isbn: 'not-an-isbn'])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books'))
                        .header('Content-Type', 'application/json')
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 422
    }

    void "the API does not emit CORS headers in the same-origin layout"() {
        when: 'a request carries an Origin header as a cross-origin browser would'
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books'))
                        .header('Origin', 'http://evil.example.com')
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString())

        then: 'no Access-Control-Allow-Origin is returned - grails.cors is not enabled (see the CORS chapter)'
        resp.statusCode() == 200
        resp.headers().firstValue('Access-Control-Allow-Origin').isEmpty()
    }

    void "GET / serves the built SPA shell with hashed asset references"() {
        when: 'requesting the SPA root (served from the Vite-built index.html)'
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/')).GET().build(),
                HttpResponse.BodyHandlers.ofString())

        then: 'the built SPA HTML is served from the same origin'
        resp.statusCode() == 200
        resp.headers().firstValue('Content-Type').get().contains('text/html')
        resp.body().contains('<div id="root"></div>')
        resp.body().contains('/assets/')
    }

    void "the hashed JS asset the SPA references is served"() {
        given: 'the asset path Vite emitted into index.html'
        HttpResponse<String> shell = client.send(
                HttpRequest.newBuilder(uri('/')).GET().build(),
                HttpResponse.BodyHandlers.ofString())
        def m = (shell.body() =~ /src="(\/assets\/[^"]+\.js)"/)
        assert m.find()
        String assetPath = m.group(1)

        when:
        HttpResponse<String> asset = client.send(
                HttpRequest.newBuilder(uri(assetPath)).GET().build(),
                HttpResponse.BodyHandlers.ofString())

        then: 'the static bundle is served from the same origin'
        asset.statusCode() == 200
    }
}

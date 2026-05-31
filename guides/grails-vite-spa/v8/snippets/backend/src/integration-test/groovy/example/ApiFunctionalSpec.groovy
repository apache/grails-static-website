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

    // ---------------------------------------------------------------------------
    // Missing CRUD endpoint coverage
    // ---------------------------------------------------------------------------

    void "GET /api/books/:id returns a single book"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780553380954')) {
                new Book(title: 'Foundation', author: 'Isaac Asimov', isbn: '9780553380954').save(failOnError: true)
            }
        }
        Long id = Book.withNewTransaction { Book.findByIsbn('9780553380954').id }

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri("/api/books/${id}")).GET().build(),
                HttpResponse.BodyHandlers.ofString())
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.title == 'Foundation'
        json.author == 'Isaac Asimov'
        json.isbn == '9780553380954'
    }

    void "GET /api/books/:id returns 404 for a non-existent book"() {
        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books/999999')).GET().build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 404
    }

    void "PUT /api/books/:id updates an existing book"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780345391803')) {
                new Book(title: 'Hitchhiker', author: 'Douglas Adams', isbn: '9780345391803').save(failOnError: true)
            }
        }
        Long id = Book.withNewTransaction { Book.findByIsbn('9780345391803').id }
        String body = JsonOutput.toJson([title: 'Hitchhikers Guide', author: 'Douglas Adams', isbn: '9780345391803'])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri("/api/books/${id}"))
                        .header('Content-Type', 'application/json')
                        .PUT(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 200
        Book.withNewTransaction {
            Book.findByIsbn('9780345391803').title == 'Hitchhikers Guide'
        }
    }

    void "PUT /api/books/:id with invalid data returns 422"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780451524935')) {
                new Book(title: '1984', author: 'George Orwell', isbn: '9780451524935').save(failOnError: true)
            }
        }
        Long id = Book.withNewTransaction { Book.findByIsbn('9780451524935').id }
        String body = JsonOutput.toJson([title: '', author: '', isbn: '9780451524935'])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri("/api/books/${id}"))
                        .header('Content-Type', 'application/json')
                        .PUT(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 422
    }

    void "PUT /api/books/:id for non-existent returns 404"() {
        given:
        String body = JsonOutput.toJson([title: 'Ghost', author: 'Nobody', isbn: '9780000000000'])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books/999999'))
                        .header('Content-Type', 'application/json')
                        .PUT(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 404
    }

    void "DELETE /api/books/:id deletes a book and returns 204"() {
        given:
        Book.withNewTransaction {
            if (!Book.findByIsbn('9780439023481')) {
                new Book(title: 'Mockingjay', author: 'Suzanne Collins', isbn: '9780439023481').save(failOnError: true)
            }
        }
        Long id = Book.withNewTransaction { Book.findByIsbn('9780439023481').id }

        when:
        HttpResponse<Void> resp = client.send(
                HttpRequest.newBuilder(uri("/api/books/${id}")).DELETE().build(),
                HttpResponse.BodyHandlers.discarding())

        then:
        resp.statusCode() == 204
        Book.withNewTransaction { Book.findByIsbn('9780439023481') == null }
    }

    void "DELETE /api/books/:id for non-existent returns 404"() {
        when:
        HttpResponse<Void> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books/999999')).DELETE().build(),
                HttpResponse.BodyHandlers.discarding())

        then:
        resp.statusCode() == 404
    }

    void "POST /api/books with blank title returns 422"() {
        given:
        String body = JsonOutput.toJson([title: '', author: 'Someone', isbn: '9780140449266'])

        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/api/books'))
                        .header('Content-Type', 'application/json')
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 422
    }

    void "GET /assets with path traversal returns 400"() {
        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/assets/../../etc/passwd')).GET().build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 400
    }

    void "GET /assets for a non-existent file returns 404"() {
        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/assets/nonexistent-bundle.js')).GET().build(),
                HttpResponse.BodyHandlers.ofString())

        then:
        resp.statusCode() == 404
    }
}

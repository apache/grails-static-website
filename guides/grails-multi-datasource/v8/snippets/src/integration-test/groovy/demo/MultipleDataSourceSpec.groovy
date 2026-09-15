package demo

import grails.testing.mixin.integration.Integration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Integration
class MultipleDataSourceSpec extends Specification {

    @Shared
    HttpClient client = HttpClient.newHttpClient()

    private HttpResponse<String> saveResource(String resource, String itemTitle, List<String> itemKeywords) {
        String body = JsonOutput.toJson([title: itemTitle, keywords: itemKeywords])
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}/${resource}"))
                .header('Content-Type', 'application/json')
                .header('Accept', 'application/json')
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> deleteResource(String resource, String itemTitle) {
        String encoded = URLEncoder.encode(itemTitle, 'UTF-8')
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}/${resource}?title=${encoded}"))
                .header('Accept', 'application/json')
                .DELETE()
                .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> fetchResource(String resource) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}/${resource}"))
                .header('Accept', 'application/json')
                .GET()
                .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> resourceKeywords(String resource) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}/${resource}/keywords"))
                .header('Accept', 'application/json')
                .GET()
                .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    def 'Test Multi-Datasource support saving and retrieving books and movies'() {
        given:
        List<Map> books = [
                [title: 'Change Agent', tags: ['dna', 'sci-fi']],
                [title: 'Influx', tags: ['sci-fi']],
                [title: 'Kill Decision', tags: ['drone', 'sci-fi']],
                [title: 'Freedom (TM)', tags: ['sci-fi']],
                [title: 'Daemon', tags: ['sci-fi']],
        ]
        List<Map> movies = [
                [title: 'Pirates of Silicon Valley', tags: ['apple', 'microsoft', 'technology']],
                [title: 'Inception', tags: ['sci-fi']],
        ]
        books.each { book ->
            HttpResponse<String> resp = saveResource('book', book.title as String, book.tags as List<String>)
            assert resp.statusCode() == 201
        }
        movies.each { movie ->
            HttpResponse<String> resp = saveResource('movie', movie.title as String, movie.tags as List<String>)
            assert resp.statusCode() == 201
        }

        when:
        HttpResponse<String> resourceResp = fetchResource('book')
        List bookBody = new JsonSlurper().parseText(resourceResp.body()) as List

        then:
        resourceResp.statusCode() == 200
        bookBody.collect { it.title }.sort() == books.collect { it.title }.sort()

        when:
        resourceResp = fetchResource('movie')
        List movieBody = new JsonSlurper().parseText(resourceResp.body()) as List

        then:
        resourceResp.statusCode() == 200
        movieBody.collect { it.title }.sort() == movies.collect { it.title }.sort()

        when:
        HttpResponse<String> resp = resourceKeywords('book')
        Map bookKeywords = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        (bookKeywords.keywords as List<String>).sort() == books.collect { it.tags }.flatten().unique().sort()

        when:
        resp = resourceKeywords('movie')
        Map movieKeywords = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        (movieKeywords.keywords as List<String>).sort() == movies.collect { it.tags }.flatten().unique().sort()

        cleanup:
        books.each { book ->
            assert deleteResource('book', book.title as String).statusCode() == 204
        }
        movies.each { movie ->
            assert deleteResource('movie', movie.title as String).statusCode() == 204
        }
    }
}

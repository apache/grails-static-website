package demo

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Shared
import spock.lang.Specification

@Integration
class MultipleDataSourceSpec extends Specification {

    @Shared
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    private HttpResponse<Map> saveResource(String resource, String itemTitle, List<String> itemKeywords) {
        HttpRequest request = HttpRequest.POST("/$resource", [title: itemTitle, keywords: itemKeywords])
        client.toBlocking().exchange(request, Map)
    }

    private HttpResponse deleteResource(String resource, String itemTitle) {
        HttpRequest request = HttpRequest.DELETE("/$resource", [title: itemTitle])
        client.toBlocking().exchange(request)
    }

    private HttpResponse<List<Map>> fetchResource(String resource) {
        HttpRequest request = HttpRequest.GET("/$resource")
        client.toBlocking().exchange(request, Argument.of(List, Map))
    }

    private HttpResponse<Map> resourceKeywords(String resource) {
        HttpRequest request = HttpRequest.GET("/$resource/keywords")
        client.toBlocking().exchange(request, Map)
    }

    def "Test Multi-Datasource support saving and retrieving books and movies"() {
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
            HttpResponse<Map> resp = saveResource('book', book.title as String, book.tags as List<String>)
            assert resp.status == HttpStatus.CREATED
        }
        movies.each { movie ->
            HttpResponse<Map> resp = saveResource('movie', movie.title as String, movie.tags as List<String>)
            assert resp.status == HttpStatus.CREATED
        }

        when:
        HttpResponse<List<Map>> resourceResp = fetchResource('book')

        then:
        resourceResp.status == HttpStatus.OK
        resourceResp.body().collect { it.title }.sort() == books.collect { it.title }.sort()

        when:
        resourceResp = fetchResource('movie')

        then:
        resourceResp.status == HttpStatus.OK
        resourceResp.body().collect { it.title }.sort() == movies.collect { it.title }.sort()

        when:
        HttpResponse<Map> resp = resourceKeywords('book')

        then:
        resp.status == HttpStatus.OK
        (resp.body().keywords as List<String>).sort() == books.collect { it.tags }.flatten().unique().sort()

        when:
        resp = resourceKeywords('movie')

        then:
        resp.status == HttpStatus.OK
        (resp.body().keywords as List<String>).sort() == movies.collect { it.tags }.flatten().unique().sort()

        cleanup:
        books.each { book ->
            assert deleteResource('book', book.title as String).status() == HttpStatus.NO_CONTENT
        }
        movies.each { movie ->
            assert deleteResource('movie', movie.title as String).status() == HttpStatus.NO_CONTENT
        }
    }
}

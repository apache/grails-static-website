package intranet.backend

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import grails.web.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification


@Integration
class AnnouncementControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    def "test body is present in announcements json payload of Api 1.0"() {
        given:
        HttpRequest request = HttpRequest.GET("/announcements/").header("Accept-Version", "1.0")

        when: 'Requesting announcements for version 1.0'
        HttpResponse<List<Map>> resp = client.toBlocking().exchange(request, List) // <1>

        then: 'the request was successful'
        resp.status == HttpStatus.OK // <3>

        and: 'the response is a JSON Payload'
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/json;charset=UTF-8'

        and: 'json payload contains an array of annoucements with id, title and body'
        resp.body().each {
            assert it.id
            assert it.title
            assert it.body // <4>
        }
    }

    def "test body is NOT present in announcements json payload of Api 2.0"() {
        given:
        HttpRequest request = HttpRequest.GET("/announcements/").header("Accept-Version", "2.0")

        when: 'Requesting announcements for version 2.0'
        HttpResponse<List<Map>> resp = client.toBlocking().exchange(request, List)

        then: 'the request was successful'
        resp.status == HttpStatus.OK // <3>

        and: 'the response is a JSON Payload'
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/json;charset=UTF-8'

        and: 'json payload contains an array of annoucements with id, title'
        resp.body().each {
            assert it.id
            assert it.title
            assert !it.body // <2>
        }
    }

    def "test detail of an announcement contains body in both version 1.0 and 2.0"() {
        given:
        def annoucementId = 2 as Long
        HttpRequest request = HttpRequest.GET("/announcements/${annoucementId}").header("Accept-Version", "1.0")

        when: 'Requesting announcements for version 1.0'
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)

        then: 'the request was successful'
        resp.status == HttpStatus.OK

        and: 'the response is a JSON Payload'
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/json;charset=UTF-8'

        and: 'json payload contains the complete annoucement'
        resp.body().id
        resp.body().title
        resp.body().body

        when: 'Requesting announcements for version 2.0'
        request = HttpRequest.GET("/announcements/${annoucementId}").header("Accept-Version", "2.0")
        resp = client.toBlocking().exchange(request, Map)

        then: 'the request was successful'
        resp.status == HttpStatus.OK

        and: 'the response is a JSON Payload'
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/json;charset=UTF-8'

        and: 'json payload contains the complete announcement'
        resp.body().id
        resp.body().title
        resp.body().body
    }
}

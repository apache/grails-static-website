package demo

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Shared
import spock.lang.Specification

@Integration
class InfoSpec extends Specification {

    @Shared
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    def "test git commit info appears in JSON"() {
        when:
        HttpResponse<Map> resp = client.toBlocking().exchange(HttpRequest.GET("/actuator/info"), Map)

        then:
        resp.status == HttpStatus.OK
        resp.body()
        resp.body().git
        resp.body().git.commit.time
        resp.body().git.commit.id
        resp.body().git.branch
    }
}

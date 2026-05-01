package app

import demo.OCI
import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@Integration
class PersonControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client = HttpClient.create(new URL(baseUrl))
    }

    def '/person endpoints return one of the names'() {
        when:
        String text = client.toBlocking().retrieve(HttpRequest.GET('/person'), String)

        then:
        OCI.PEOPLE.contains text
    }
}

package example.grails

import grails.testing.mixin.integration.Integration
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import grails.testing.spock.OnceBefore

@Integration
class HealthSpec extends Specification {

    @Shared
    @AutoCleanup
    HttpClient client

    @OnceBefore // <1>
    void init() {
        String baseUrl = "http://localhost:$serverPort" // <2>
        client = HttpClient.create(new URL(baseUrl))
    }

    void "health responds OK"()  {
        when:
        Map m = client.toBlocking().retrieve(HttpRequest.GET("/health"), Map) // <3>

        then:
        m
        m.containsKey("status")
        m.get("status") ==  "UP"
    }
}

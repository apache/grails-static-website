package example.grails

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Specification

@Integration
class HomeControllerSpec extends Specification {

    UserDataService userDataService
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    def "verify basic auth is possible"() {
        given:
        final String username = 'sherlock'
        final String password = 'elementary'

        when:
        User user = userDataService.save(username, password, true, false, false, false, )

        then:
        userDataService.count() == old(userDataService.count()) + 1

        when:
        HttpRequest request = HttpRequest.GET("/home")
                .basicAuth(username, password)
        HttpResponse<Map> rsp = client.toBlocking().exchange(request, Map)

        then:
        rsp.status == HttpStatus.OK

        and:
        rsp.body().username == username

        cleanup:
        userDataService?.delete(user?.id)
    }
}
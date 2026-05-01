package demo

import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Specification
import grails.testing.mixin.integration.Integration

@SuppressWarnings(['LineLength', 'MethodName'])
@Integration
class PlayerControllerFuncSpec extends Specification {

    @Shared
    @AutoCleanup
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    def 'test save validation'() {
        when:

        Map<String, Object> json = [name : 'Bob Smith',
                                    wins : 42,
                                    losses : 'abc']
        HttpResponse<Map> resp = client.toBlocking().exchange(HttpRequest.POST("/player/save", json), Map)
               then:
        resp.status == HttpStatus.UNPROCESSABLE_ENTITY // <4>
        resp.body().errors.size() == 2
        resp.body().errors.find { it.field == 'losses' }.message == 'Property losses is type-mismatched'
        resp.body().errors.find { it.field == 'game' }.message == 'Property [game] of class [class demo.Player] cannot be null'
    }
}

package example.grails

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class HomeControllerSpec extends Specification {

    UserDataService userDataService

    def "verify basic auth is possible"() {
        given:
        RestBuilder rest = new RestBuilder()
        final String username = 'sherlock'
        final String password = 'elementary'

        when:
        User user = userDataService.save(username, password, true, false, false, false, )

        then:
        userDataService.count() == old(userDataService.count()) + 1

        when:
        String token = "${username}:${password}".encodeAsBase64()
        RestResponse rsp = rest.get("http://localhost:${serverPort}/home") {
            auth("Basic $token")
        }

        then:
        rsp.status == 200

        and:
        rsp.json.username == username

        cleanup:
        userDataService?.delete(user?.id)
    }
}
package demo

import spock.lang.Specification
import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration

@SuppressWarnings(['LineLength', 'MethodName'])
@Integration
class PlayerControllerFuncSpec extends Specification {

    def 'test save validation'() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def resp = rest.post("http://localhost:${serverPort}/player/save") { // <1>
            accept('application/json') // <2>
            contentType('application/json') // <3>
            json {
                name = 'Bob Smith'
                wins = 42
                losses = 'abc'
            }
        }

        then:
        resp.status == 422 // <4>
        resp.json.errors.size() == 2
        resp.json.errors.find { it.field == 'losses' }.message == 'Property losses is type-mismatched'
        resp.json.errors.find { it.field == 'game' }.message == 'Property [game] of class [class demo.Player] cannot be null'
    }
}

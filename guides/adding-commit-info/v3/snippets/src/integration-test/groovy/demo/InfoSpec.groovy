package demo

import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
class InfoSpec extends Specification {

    def "test git commit info appears in JSON"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def resp = rest.get("http://localhost:${serverPort}/info") {
            header("Accept", "application/json")
        }

        then:
        resp.statusCode.value() == 200
        resp.json
        resp.json.git
        resp.json.git.commit
        resp.json.git.commit.message
        resp.json.git.commit.time
        resp.json.git.commit.id
        resp.json.git.commit.user
        resp.json.git.branch
    }
}

package demo

import grails.gorm.multitenancy.Tenants
import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf( { System.getenv('TRAVIS') as boolean } )
@Integration
class PlanControllerSpec extends Specification {
    PlanDataService planDataService
    UserDataService userDataService
    UserRoleDataService userRoleDataService
    UserService userService
    RoleDataService roleDataService

    RestBuilder rest = new RestBuilder()

    String accessToken(String u, String p) {
        def resp = rest.post("http://localhost:${serverPort}/api/login") {
            accept('application/json')
            contentType('application/json')
            json {
                username = u
                password = p
            }
        }
        if ( resp.status == 200 ) {
            return resp.json.access_token
        }
        null
    }

    def "Plans for current logged user are retrieved"() {
        given:
        User vector = userService.save('vector', 'secret', 'ROLE_VILLAIN')
        User gru = userService.save('gru', 'secret', 'ROLE_VILLAIN')
        Tenants.withId("gru") { // <1>
            planDataService.save('Steal the Moon')
        }
        Tenants.withId("vector") {
            planDataService.save('Steal a Pyramid')
        }

        when: 'login with the gru'
        String gruAccessToken = accessToken('gru', 'secret')

        then:
        gruAccessToken

        when:
        def resp = rest.get("http://localhost:${serverPort}/plan") {
            accept('application/json')
            header('Authorization', "Bearer ${gruAccessToken}")
        }

        then:
        resp.status == 200
        resp.json.toString() == '[{"title":"Steal the Moon"}]'

        when: 'login with the vector'
        String vectorAccessToken = accessToken('vector', 'secret')

        then:
        vectorAccessToken

        when:
        resp = rest.get("http://localhost:${serverPort}/plan") {
            accept('application/json')
            header('Authorization', "Bearer ${vectorAccessToken}")
        }

        then:
        resp.status == 200
        resp.json.toString() == '[{"title":"Steal a Pyramid"}]'

        cleanup:
        Tenants.withId("gru") { // <1>
            planDataService.deleteByTitle('Steal the Moon')
        }
        Tenants.withId("vector") {
            planDataService.deleteByTitle('Steal the Pyramid')
        }
        userRoleDataService.deleteByUser(gru)
        userDataService.delete(gru.id)
        userRoleDataService.deleteByUser(vector)
        userDataService.delete(vector.id)
        roleDataService.deleteByAuthority('ROLE_VILLAIN')
    }
}

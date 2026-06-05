package demo

import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Shared
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

    @Shared
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    String accessToken(String u, String p) {
        HttpRequest request = HttpRequest.POST("/api/login", [username: u, password: p])
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)
        if ( resp.status == HttpStatus.OK) {
            return resp.body().access_token
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
        HttpRequest request = HttpRequest.GET("/plan").bearerAuth(gruAccessToken)
        HttpResponse<String> resp = client.toBlocking().exchange(request, String)

        then:
        resp.status == HttpStatus.OK
        resp.body() == '[{"title":"Steal the Moon"}]'

        when: 'login with the vector'
        String vectorAccessToken = accessToken('vector', 'secret')

        then:
        vectorAccessToken

        when:
        request = HttpRequest.GET("/plan").bearerAuth(vectorAccessToken)
        resp = client.toBlocking().exchange(request, String)

        then:
        resp.status == HttpStatus.OK
        resp.body() == '[{"title":"Steal a Pyramid"}]'

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

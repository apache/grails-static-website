package example.grails

import grails.testing.mixin.integration.Integration
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientException
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import grails.testing.spock.OnceBefore

@SuppressWarnings(['MethodName', 'DuplicateNumberLiteral', 'Instanceof'])
@Integration
class ApiAnnouncementControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    HttpClient client

    @OnceBefore // <1>
    void init() {
        client  = HttpClient.create(new URL("http://localhost:$serverPort")) // <2>
    }

    def 'test /api/announcements url is secured'() {
        when:
        HttpRequest request = HttpRequest.GET('/api/announcements')
        client.toBlocking().exchange(request,  // <3>
                Argument.of(List, AnnouncementView),
                Argument.of(CustomError))

        then:
        HttpClientException e = thrown(HttpClientException)
        e.response.status == HttpStatus.UNAUTHORIZED // <4>

        when:
        Optional<CustomError> jsonError = e.response.getBody(CustomError)

        then:
        jsonError.isPresent()
        jsonError.get().status == 401
        jsonError.get().error == 'Unauthorized'
        jsonError.get().message == null
        jsonError.get().path == '/api/announcements'
    }

    def "test a user with the role ROLE_BOSS is able to access /api/announcements url"() {
        when: 'login with the sherlock'
        UserCredentials credentials = new UserCredentials(username: 'sherlock', password: 'elementary')
        HttpRequest request = HttpRequest.POST('/api/login', credentials) // <5>
        HttpResponse<BearerToken> resp = client.toBlocking().exchange(request, BearerToken)

        then:
        resp.status.code == 200
        resp.body().roles.find { it == 'ROLE_BOSS' }

        when:
        String accessToken = resp.body().accessToken

        then:
        accessToken

        when:
        HttpResponse<List> rsp = client.toBlocking().exchange(HttpRequest.GET('/api/announcements')
                .header('Authorization', "Bearer ${accessToken}"), Argument.of(List, AnnouncementView)) // <6>

        then:
        rsp.status.code == 200 // <7>
        rsp.body() != null
        ((List)rsp.body()).size() == 1
        ((List)rsp.body()).get(0) instanceof AnnouncementView
        ((AnnouncementView) ((List)rsp.body()).get(0)).message == 'The Hound of the Baskervilles'
    }

    def "test a user with the role ROLE_EMPLOYEE is NOT able to access /api/announcements url"() {
        when: 'login with the watson'

        UserCredentials creds = new UserCredentials(username: 'watson', password: '221Bbakerstreet')
        HttpRequest request = HttpRequest.POST('/api/login', creds)
        HttpResponse<BearerToken> resp = client.toBlocking().exchange(request, BearerToken)

        then:
        resp.status.code == 200
        !resp.body().roles.find { it == 'ROLE_BOSS' }
        resp.body().roles.find { it == 'ROLE_EMPLOYEE' }

        when:
        String accessToken = resp.body().accessToken

        then:
        accessToken

        when:
        resp = client.toBlocking().exchange(HttpRequest.GET('/api/announcements')
                .header('Authorization', "Bearer ${accessToken}"))

        then:
        def e = thrown(HttpClientException)
        e.response.status == HttpStatus.FORBIDDEN // <8>
    }
}

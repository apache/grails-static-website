package example.grails

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpStatus
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Shared
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@Integration
class MailControllerSpec extends Specification {

    @Shared
    HttpClient client

    EmailService emailService

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    def "/mail/send interacts once email service"() {
        when:
        HttpRequest request = HttpRequest.POST('/mail/send', [
                subject: 'Test',
                recipient: 'delamos@grails.example',
                textBody: 'Hola hola'
        ])
        HttpResponse resp = client.toBlocking().exchange(request)

        then:
        resp.status == HttpStatus.OK
        1 * emailService.send(_) // <1>
    }

    @TestConfiguration
    static class EmailServiceConfiguration {
        private DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        EmailService emailService() {
            factory.Mock(EmailService)
        }
    }
}

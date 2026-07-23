package example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@Integration
class MailControllerSpec extends Specification {

    @Autowired
    EmailService emailService

    void '/mail/send invokes email service with expected message'() {
        when:
        RestTemplate client = new RestTemplate()
        ResponseEntity<Map> resp = client.postForEntity(
                "http://localhost:$serverPort/mail/send",
                [
                        subject  : 'Test',
                        recipient: 'delamos@grails.example',
                        textBody : 'Hola hola'
                ],
                Map
        )

        then:
        resp.statusCode == HttpStatus.OK
        1 * emailService.send({ Email email ->
            email.recipient == 'delamos@grails.example' &&
                    email.subject == 'Test' &&
                    email.textBody == 'Hola hola'
        }) // <1>
    }

    void 'invalid request without recipient returns 422 with validation errors'() {
        when:
        RestTemplate client = new RestTemplate()
        HttpClientErrorException ex = null
        try {
            client.postForEntity(
                    "http://localhost:$serverPort/mail/send",
                    [
                            subject : 'Test',
                            textBody: 'Hola hola'
                    ],
                    Map
            )
        } catch (HttpClientErrorException e) {
            ex = e
        }

        then:
        ex != null
        ex.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
        def body = new JsonSlurper().parseText(ex.responseBodyAsString)
        body.message
        body.path
        body._links
        0 * emailService.send(_)
    }

    void 'invalid request without subject returns 422 with validation errors'() {
        when:
        RestTemplate client = new RestTemplate()
        HttpClientErrorException ex = null
        try {
            client.postForEntity(
                    "http://localhost:$serverPort/mail/send",
                    [
                            recipient: 'delamos@grails.example',
                            textBody : 'Hola hola'
                    ],
                    Map
            )
        } catch (HttpClientErrorException e) {
            ex = e
        }

        then:
        ex != null
        ex.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
        def body = new JsonSlurper().parseText(ex.responseBodyAsString)
        body.message
        body.path
        body._links
        0 * emailService.send(_)
    }

    @TestConfiguration
    static class EmailServiceConfiguration {
        private DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        @Primary
        EmailService emailService() {
            factory.Mock(EmailService)
        }
    }
}

package example.grails

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.Body
import software.amazon.awssdk.services.ses.model.Content
import software.amazon.awssdk.services.ses.model.Destination
import software.amazon.awssdk.services.ses.model.Message
import software.amazon.awssdk.services.ses.model.SendEmailRequest
import software.amazon.awssdk.services.ses.model.SendEmailResponse

@Slf4j
@CompileStatic
class AwsSesMailService implements EmailService, GrailsConfigurationAware {  // <1>

    String sourceEmail

    SesClient sesClient

    @Override
    void setConfiguration(Config co) {

        String awsRegion = co.getProperty('aws.ses.region')
        if (!awsRegion) {
            throw new IllegalStateException('aws.ses.region not set')
        }
        this.sesClient = SesClient.builder().region(Region.of(awsRegion)).build();

        this.sourceEmail = co.getProperty('aws.ses.source', '')
        if (!this.sourceEmail) {
            log.warn('aws.sourceEmail not set')
        }
    }

    private Body bodyOfEmail(Email email) {
        if (email.htmlBody) {
            Content htmlBody = Content.builder().data(email.htmlBody).build()
            return Body.builder().html(htmlBody).build()
        }
        if (email.textBody) {
            Content textBody = Content.builder().data(email.textBody).build()
            return Body.builder().text(textBody).build()
        }
        Body.builder().build()
    }

    private Destination destination(Email email) {
        Destination.Builder destinationBuilder = Destination.builder().toAddresses(email.recipient)
        if ( email.getCc() ) {
            destinationBuilder = destinationBuilder.ccAddresses(email.getCc())
        }
        if ( email.getBcc() ) {
            destinationBuilder = destinationBuilder.bccAddresses(email.getBcc())
        }
        destinationBuilder.build()
    }

    private Message composeMessage(Email email) {
        Content subject = Content.builder().data(email.getSubject()).build()
        Body body = bodyOfEmail(email)
        Message.builder().subject(subject).body(body).build()
    }

    @Override
     void send(Email email) {
        try {
            Destination destination = destination(email)
            Message message = composeMessage(email)
            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                    .source(sourceEmail)
                    .destination(destination)
                    .message(message)
                    .build()
            SendEmailResponse response = sesClient.sendEmail(sendEmailRequest)
            log.info("Email sent! {}", response.messageId())

        } catch (Exception ex) {
            log.warn("The email was not sent.")
            log.warn("Error message: {}", ex.message)
        }
    }
}

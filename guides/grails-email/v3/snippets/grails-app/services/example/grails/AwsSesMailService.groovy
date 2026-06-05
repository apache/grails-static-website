package example.grails

import com.amazonaws.services.simpleemail.model.SendEmailResult
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest

@Slf4j
@CompileStatic
class AwsSesMailService implements EmailService, GrailsConfigurationAware {  // <1>

    String awsRegion

    String sourceEmail

    AwsCredentialsProviderService awsCredentialsProviderService

    @Override
    void setConfiguration(Config co) {
        this.awsRegion = co.getProperty('aws.ses.region')
        if (!this.awsRegion) {
            throw new IllegalStateException('aws.ses.region not set')
        }

        this.sourceEmail = co.getProperty('aws.sourceEmail')
        if (!this.sourceEmail) {
            throw new IllegalStateException('aws.sourceEmaill not set')
        }
    }

    private Body bodyOfEmail(Email email) {
        if (email.htmlBody) {
            Content htmlBody = new Content().withData(email.htmlBody)
            return new Body().withHtml(htmlBody)
        }
        if (email.textBody) {
            Content textBody = new Content().withData(email.textBody)
            return new Body().withHtml(textBody)
        }
        new Body()
    }

    @Override
     void send(Email email) {

        if ( !awsCredentialsProviderService ) {
            log.warn("AWS Credentials provider not configured")
            return
        }

        Destination destination = new Destination().withToAddresses(email.recipient)
        if ( email.getCc() ) {
            destination = destination.withCcAddresses(email.getCc())
        }
        if ( email.getBcc() ) {
            destination = destination.withBccAddresses(email.getBcc())
        }
        Content subject = new Content().withData(email.getSubject())
        Body body = bodyOfEmail(email)
        Message message = new Message().withSubject(subject).withBody(body)

        SendEmailRequest request = new SendEmailRequest()
                .withSource(sourceEmail)
                .withDestination(destination)
                .withMessage(message)

        if ( email.getReplyTo() ) {
            request = request.withReplyToAddresses()
        }

        try {
            log.info("Attempting to send an email through Amazon SES by using the AWS SDK for Java...")

            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(awsCredentialsProviderService)
                    .withRegion(awsRegion)
                    .build()

            SendEmailResult sendEmailResult = client.sendEmail(request)
            log.info("Email sent! {}", sendEmailResult.toString())

        } catch (Exception ex) {
            log.warn("The email was not sent.")
            log.warn("Error message: {}", ex.message)
        }
    }
}

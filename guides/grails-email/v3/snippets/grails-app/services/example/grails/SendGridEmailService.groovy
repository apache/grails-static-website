package example.grails

import com.sendgrid.Personalization
import com.sendgrid.Content
import com.sendgrid.Mail
import com.sendgrid.SendGrid
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.Method
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class SendGridEmailService implements EmailService, GrailsConfigurationAware {  // <1>

    String apiKey

    String fromEmail

    @Override
    void setConfiguration(Config co) {
        this.apiKey = co.getProperty('sendgrid.apiKey', String)
        if (!this.apiKey) {
            throw new IllegalStateException('sendgrid.apiKey not set')
        }
        this.fromEmail = co.getProperty('sendgrid.fromEmail', String)
        if (!this.fromEmail) {
            throw new IllegalStateException('sendgrid.apiKey not set')
        }
    }

    protected Content contentOfEmail(Email email) {
        if ( email.textBody ) {
            return new Content("text/plain", email.textBody)
        }
        if ( email.htmlBody ) {
            return new Content("text/html", email.htmlBody)
        }
        return null
    }

    @Override
    void send(Email email) {

        Personalization personalization = new Personalization()
        personalization.subject = email.subject

        com.sendgrid.Email to = new com.sendgrid.Email(email.recipient)
        personalization.addTo(to)

        if ( email.getCc() ) {
            for ( String cc : email.getCc() ) {
                com.sendgrid.Email ccEmail = new com.sendgrid.Email()
                ccEmail.email = cc
                personalization.addCc(ccEmail)
            }
        }

        if ( email.getBcc() ) {
            for ( String bcc : email.getBcc() ) {
                com.sendgrid.Email bccEmail = new com.sendgrid.Email()
                bccEmail.email = bcc
                personalization.addBcc(bccEmail)
            }
        }

        Mail mail = new Mail()
        com.sendgrid.Email from = new com.sendgrid.Email()
        from.email = fromEmail
        mail.from = from
        mail.addPersonalization(personalization)
        Content content = contentOfEmail(email)
        mail.addContent(content)

        SendGrid sg = new SendGrid(apiKey)
        Request request = new Request()
        try {
            request.with {
                method = Method.POST
                endpoint = "mail/send"
                body = mail.build()
            }
            Response response = sg.api(request)
            log.info("Status Code: {}", String.valueOf(response.getStatusCode()))
            log.info("Body: {}", response.getBody())
            if ( log.infoEnabled ) {
                response.getHeaders().each { String k, String v ->
                    log.info("Response Header {} => {}", k, v)
                }
            }

        } catch (IOException ex) {
            log.error(ex.getMessage())
        }
    }
}
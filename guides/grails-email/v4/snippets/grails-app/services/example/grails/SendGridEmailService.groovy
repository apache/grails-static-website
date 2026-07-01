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

    String api

    String from

    @Override
    void setConfiguration(Config co) {
        this.api = co.getProperty('sendgrid.api', String)
        if (!this.api) {
            throw new IllegalStateException('sendgrid.api not set')
        }
        this.from = co.getProperty('sendgrid.from', String)
        if (!this.from) {
            throw new IllegalStateException('sendgrid.apiKey not set')
        }
    }

    @Override
    void send(Email email) {
        Mail mail = buildEmail(email)
        SendGrid sg = new SendGrid(api)
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
    
    private Content contentOfEmail(Email email) {
        if ( email.textBody ) {
            return new Content("text/plain", email.textBody)
        }
        if ( email.htmlBody ) {
            return new Content("text/html", email.htmlBody)
        }
        return null
    }

    private Personalization buildPersonalization(Email email) {
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
        personalization
    }

    private Mail buildEmail(Email email) {
        Personalization personalization = buildPersonalization(email)
        Mail mail = new Mail()
        com.sendgrid.Email from = new com.sendgrid.Email()
        from.email = from
        mail.from = from
        mail.addPersonalization(personalization)
        Content content = contentOfEmail(email)
        mail.addContent(content)
        mail
    }
}
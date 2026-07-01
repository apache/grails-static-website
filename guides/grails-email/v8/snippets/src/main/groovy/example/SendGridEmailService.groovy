package example

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email as SendGridEmail
import com.sendgrid.helpers.mail.objects.Personalization
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
            throw new IllegalStateException('sendgrid.from not set')
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
                endpoint = 'mail/send'
                body = mail.build()
            }
            Response response = sg.api(request)
            log.info('Status Code: {}', String.valueOf(response.statusCode))
            log.debug('Body: {}', response.body)
        } catch (IOException ex) {
            log.error(ex.message)
        }
    }

    private Content contentOfEmail(Email email) {
        if (email.textBody) {
            return new Content('text/plain', email.textBody)
        }
        if (email.htmlBody) {
            return new Content('text/html', email.htmlBody)
        }
        return null
    }

    private Personalization buildPersonalization(Email email) {
        Personalization personalization = new Personalization()
        personalization.subject = email.subject

        SendGridEmail to = new SendGridEmail(email.recipient)
        personalization.addTo(to)

        if (email.cc) {
            for (String cc : email.cc) {
                personalization.addCc(new SendGridEmail(cc))
            }
        }
        if (email.bcc) {
            for (String bcc : email.bcc) {
                personalization.addBcc(new SendGridEmail(bcc))
            }
        }
        personalization
    }

    private Mail buildEmail(Email email) {
        Personalization personalization = buildPersonalization(email)
        Mail mail = new Mail()
        SendGridEmail fromEmail = new SendGridEmail(this.from)
        mail.from = fromEmail
        mail.addPersonalization(personalization)
        Content content = contentOfEmail(email)
        mail.addContent(content)
        mail
    }
}

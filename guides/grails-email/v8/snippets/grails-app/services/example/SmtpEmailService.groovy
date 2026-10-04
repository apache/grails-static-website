package example

import grails.plugins.mail.MailService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class SmtpEmailService implements EmailService {  // <1>

    MailService mailService

    @Override
    void send(Email email) {
        mailService.sendMail {
            to email.recipient
            subject email.subject
            if (email.textBody) {
                text email.textBody
            }
            if (email.htmlBody) {
                html email.htmlBody
            }
            if (email.replyTo) {
                replyTo email.replyTo
            }
            if (email.cc) {
                cc email.cc as String[]
            }
            if (email.bcc) {
                bcc email.bcc as String[]
            }
        }
        log.debug('SMTP message handed off to MailService for {}', email.recipient)
    }
}

package example.grails

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class MailController {

    EmailService emailService

    static allowedMethods = [send: 'POST']

    def send(EmailCmd cmd) {
        if ( cmd.hasErrors() ) {
            render status: 422
            return
        }
        log.info '{}', cmd.toString()
        emailService.send(cmd)
        render status: 200
    }
}
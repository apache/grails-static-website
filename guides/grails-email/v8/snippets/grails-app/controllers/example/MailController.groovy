package example

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

@Slf4j
@CompileStatic
class MailController {

    EmailService emailService

    static allowedMethods = [send: 'POST']

    def send(EmailCmd cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, view: '/application/errors', status: UNPROCESSABLE_ENTITY
            return
        }
        log.info '{}', cmd.toString()
        emailService.send(cmd)
        render status: 200
    }
}

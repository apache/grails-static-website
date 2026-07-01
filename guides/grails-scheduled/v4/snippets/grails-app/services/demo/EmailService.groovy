package demo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat

@Slf4j
@CompileStatic
class EmailService {

    void send(String user, String message) {
        log.info "Sending email to {} : {} at {}",
                user, message, new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date())
    }
}

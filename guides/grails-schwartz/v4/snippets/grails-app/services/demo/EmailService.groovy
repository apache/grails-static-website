package demo

import groovy.transform.CompileStatic

@CompileStatic
class EmailService {

    void send(String user) {
        log.info "Sending email to ${user}"
    }
}

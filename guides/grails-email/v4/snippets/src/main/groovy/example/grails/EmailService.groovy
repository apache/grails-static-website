package example.grails

import groovy.transform.CompileStatic

@CompileStatic
interface EmailService {
    void send(Email email)
}
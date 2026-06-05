package example.grails

import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable
import groovy.transform.ToString

@ToString
@GrailsCompileStatic
class EmailCmd implements Validateable, Email {
    String recipient
    List<String> cc = []
    List<String> bcc = []
    String subject
    String htmlBody
    String textBody
    String replyTo

    static constraints = {
        recipient nullable: false // <1>
        subject nullable: false  // <2>
        htmlBody nullable: true
        textBody nullable: true, validator: { String val, EmailCmd obj ->  // <3>
            !(!obj.htmlBody && !val)
        }
        replyTo nullable: true
    }
}

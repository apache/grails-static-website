package example.grails

import groovy.transform.CompileStatic

@CompileStatic
interface Email {
    String getRecipient()
    List<String> getCc()
    List<String> getBcc()
    String getSubject()
    String getHtmlBody()
    String getTextBody()
    String getReplyTo()
}
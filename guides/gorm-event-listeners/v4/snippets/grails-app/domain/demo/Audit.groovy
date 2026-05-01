package demo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Audit {

    String event
    Long bookId

    static constraints = {
        event nullable: false, blank: false
        bookId nullable: false
    }
}

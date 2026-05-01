package demo

import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@GrailsCompileStatic
class SaveBookCommand implements Validateable {
    String title
    List<String> keywords

    static constraints = {
        title nullable: false
        keywords nullable: true
    }
}
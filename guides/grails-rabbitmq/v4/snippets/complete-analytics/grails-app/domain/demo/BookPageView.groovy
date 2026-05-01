package demo

import grails.compiler.GrailsCompileStatic
import grails.rest.Resource

@Resource
@GrailsCompileStatic
class BookPageView {
    Long bookId
    String bookName
    Integer views

    static constraints = {
        bookId nullable: false, unique: true
        bookName nullable: false
        views nullable: false, min: 0
    }
}
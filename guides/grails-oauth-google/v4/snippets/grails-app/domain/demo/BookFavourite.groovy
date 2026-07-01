package demo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class BookFavourite {
    Long bookId
    String username

    static constraints = {
        bookId nullable: false
        username nullable: false
    }
}
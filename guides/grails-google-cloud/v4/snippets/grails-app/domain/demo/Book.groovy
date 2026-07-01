package demo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Book {
    String name
    String featuredImageUrl
    String fileName

    static constraints = {
        name unique: true
        featuredImageUrl nullable: true
        fileName nullable: true
    }
}
package demo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Book {
    String image
    String title
    String author
    String about
    String href
    static mapping = {
        image nullable: false
        title nullable: false
        author nullable: false
        about nullable: false
        href nullable: false
        about type: 'text'
    }
}
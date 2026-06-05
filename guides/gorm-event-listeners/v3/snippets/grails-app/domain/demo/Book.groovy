package demo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Book {

    String author
    String title
    String friendlyUrl
    Integer pages
    String serialNumber

    static constraints = {
        serialNumber nullable: true
        friendlyUrl nullable: true
        title nullable: false
        pages min: 0
        serialNumber nullable: true
    }
}

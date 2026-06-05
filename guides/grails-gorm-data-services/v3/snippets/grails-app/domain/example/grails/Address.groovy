package example.grails

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Address {
    String streetName
    String city
    String state
    String country

    static belongsTo = [person: Person]
}

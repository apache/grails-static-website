package demo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Address {
    Person person
    String streetName
    String city
    String zipCode

    static belongsTo = [person: Person]

    static constraints = {
        streetName nullable: true
        city nullable: true
        zipCode nullable: true
    }
}

package example

import grails.persistence.Entity

@Entity
class Address {

    Person person
    String streetName
    String city
    String zipCode

    static belongsTo = [person: Person]

    static mapping = {
        id generator: 'identity'
    }

    static constraints = {
        streetName nullable: true
        city nullable: true
        zipCode nullable: true
    }
}

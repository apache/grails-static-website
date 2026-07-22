package example

import grails.persistence.Entity

@Entity
class Person {

    String name
    Integer age

    static hasMany = [addresses: Address]

    static mapping = {
        id generator: 'identity'
    }

    static constraints = {
        name nullable: false
        age nullable: true
    }
}

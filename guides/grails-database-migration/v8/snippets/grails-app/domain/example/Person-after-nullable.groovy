package example

import grails.persistence.Entity

@Entity
class Person {

    String name
    Integer age

    static constraints = {
        name nullable: false
        age nullable: true
    }
}

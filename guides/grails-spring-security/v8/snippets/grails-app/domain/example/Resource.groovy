package example

import grails.persistence.Entity

@Entity
class Resource {

    String name
    String description

    static constraints = {
        name blank: false, nullable: false, maxSize: 255
        description nullable: true, maxSize: 1000
    }

    String toString() {
        name
    }
}

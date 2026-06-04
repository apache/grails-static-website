package example

import grails.persistence.Entity

@Entity
class Author {

    String name
    String email

    static hasMany = [books: Book]

    static constraints = {
        name blank: false, maxSize: 100
        email email: true, unique: true, nullable: true, maxSize: 255
    }

    String toString() {
        name
    }
}

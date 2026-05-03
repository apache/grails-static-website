package example

import grails.persistence.Entity

@Entity
class Author {

    String name
    String biography
    Date dateOfBirth

    static hasMany = [books: Book]

    static constraints = {
        name        blank: false, maxSize: 255
        biography   nullable: true, maxSize: 4000
        dateOfBirth nullable: true
    }

    static mapping = {
        books fetch: 'lazy'
    }

    String toString() { name }
}

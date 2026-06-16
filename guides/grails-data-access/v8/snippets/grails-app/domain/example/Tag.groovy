package example

import grails.persistence.Entity

@Entity
class Tag {

    String name

    static hasMany = [books: Book]

    static constraints = {
        name blank: false, unique: true, maxSize: 50
    }

    String toString() {
        name
    }
}

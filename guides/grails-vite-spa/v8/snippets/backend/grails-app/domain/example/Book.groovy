package example

import grails.persistence.Entity

@Entity
class Book {

    String  title
    String  author
    String  isbn

    static constraints = {
        title  nullable: false, blank: false, maxSize: 255
        author nullable: false, blank: false, maxSize: 255
        isbn   nullable: false, blank: false, unique: true, matches: /^(97(8|9))?\d{9}(\d|X)$/
    }
}

package example

import grails.persistence.Entity

@Entity
class Book {

    String  title
    String  author
    String  isbn

    static constraints = {
        title  blank: false, maxSize: 255
        author blank: false, maxSize: 255
        isbn   blank: false, unique: true, matches: /^(97(8|9))?\d{9}(\d|X)$/
    }
}

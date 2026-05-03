package example

import grails.persistence.Entity

@Entity
class Book {

    String  title
    String  isbn
    Integer pageCount
    Date    publishedOn

    static belongsTo = [author: Author]

    static constraints = {
        title       blank: false, maxSize: 255
        isbn        blank: false, unique: true, matches: /^(97(8|9))?\d{9}(\d|X)$/
        pageCount   nullable: true, min: 1
        publishedOn nullable: true
    }

    String toString() { title }
}

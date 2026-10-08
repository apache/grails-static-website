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
        author      nullable: false
        title       nullable: false, blank: false, maxSize: 255
        isbn        nullable: false, blank: false, unique: true, maxSize: 20, matches: /^(97(8|9))?\d{9}(\d|X)$/
        pageCount   nullable: true, min: 1
        publishedOn nullable: true
    }

    String toString() { title }
}

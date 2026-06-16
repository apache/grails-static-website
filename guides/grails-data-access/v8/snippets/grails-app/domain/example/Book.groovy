package example

import grails.persistence.Entity

import java.time.LocalDate

@Entity
class Book {

    String title
    String isbn
    BigDecimal price
    LocalDate publishedOn
    Author author

    static belongsTo = [author: Author]

    static hasMany = [tags: Tag]

    static mapping = {
        tags joinTable: [name: 'book_tag', key: 'book_id', column: 'tag_id']
    }

    static constraints = {
        title blank: false, maxSize: 255
        isbn nullable: true, maxSize: 20
        price nullable: false, min: 0.01G, scale: 2
        publishedOn nullable: true
        author nullable: false
    }

    String toString() {
        title
    }
}

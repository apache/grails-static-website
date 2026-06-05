package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * Domain constraint unit tests for Book - no Spring context, no database.
 */
class BookSpec extends Specification implements DomainUnitTest<Book> {

    void "a fully populated book validates"() {
        expect:
        new Book(title: 'Dune', author: 'Frank Herbert', isbn: '9780441013593').validate()
    }

    void "title, author and isbn are required"() {
        when:
        Book b = new Book()

        then:
        !b.validate()
        b.errors.getFieldError('title').code == 'nullable'
        b.errors.getFieldError('author').code == 'nullable'
        b.errors.getFieldError('isbn').code == 'nullable'
    }

    void "isbn must match the ISBN pattern"() {
        when:
        Book b = new Book(title: 'X', author: 'Y', isbn: 'not-an-isbn')

        then:
        !b.validate()
        b.errors.getFieldError('isbn').code == 'matches.invalid'
    }

    void "isbn must be unique"() {
        given:
        new Book(title: 'First', author: 'A', isbn: '9780441013593').save(flush: true, failOnError: true)

        when:
        Book dup = new Book(title: 'Second', author: 'B', isbn: '9780441013593')

        then:
        !dup.validate()
        dup.errors.getFieldError('isbn').code == 'unique'
    }
}

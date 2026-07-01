package example

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Domain constraint unit tests for Book. DataTest mocks both Book and its
 * owning Author so the belongsTo association can be satisfied without a
 * Spring context or a real database.
 */
class BookSpec extends Specification implements DataTest {

    Class[] getDomainClassesToMock() { [Author, Book] }

    private Book bookWith(Map overrides) {
        Author author = new Author(name: 'Author').save(flush: true, failOnError: true)
        new Book([author: author, title: 'A Title', isbn: '9780547928227', pageCount: 100] + overrides)
    }

    void "a fully populated book validates"() {
        expect:
        bookWith([:]).validate()
    }

    void "author is required"() {
        when:
        Book b = new Book(title: 'No Author', isbn: '9780547928227')

        then:
        !b.validate()
        b.errors.getFieldError('author').code == 'nullable'
    }

    void "title is required"() {
        when:
        Book b = bookWith(title: null)

        then:
        !b.validate()
        b.errors.getFieldError('title').code == 'nullable'
    }

    void "isbn must match the ISBN pattern"() {
        when:
        Book b = bookWith(isbn: 'not-an-isbn')

        then:
        !b.validate()
        b.errors.getFieldError('isbn').code == 'matches.invalid'
    }

    void "isbn must be unique"() {
        given:
        bookWith(isbn: '9780547928227').save(flush: true, failOnError: true)

        when:
        Book dup = bookWith(isbn: '9780547928227')

        then:
        !dup.validate()
        dup.errors.getFieldError('isbn').code == 'unique'
    }

    @Unroll
    void "pageCount #pageCount is #valid"() {
        expect:
        bookWith(pageCount: pageCount).validate() == valid

        where:
        pageCount || valid
        null      || true
        1         || true
        500       || true
        0         || false
        -5        || false
    }
}

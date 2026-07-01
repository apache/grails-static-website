package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * DomainUnitTest<Book> exercises just the constraint logic.
 *
 * Loads in milliseconds because no Spring context boots; the
 * @AutoCleanup datastore is in-memory and per-spec. Use this layer for
 * every constraint regression: blank, nullable, maxSize, unique,
 * matches, min, max, range.
 */
class BookSpec extends Specification implements DomainUnitTest<Book> {

    void "rejects a book with no title"() {
        when: 'title is omitted from the map constructor, so it is null'
        Book b = new Book(isbn: '9780547928227', pageCount: 310)

        then: 'the nullable constraint - not blank - is what fires for a null value'
        !b.validate()
        b.errors.getFieldError('title').code == 'nullable'
    }

    void "rejects a blank title"() {
        when: 'a blank string is assigned directly (the map constructor would convert it to null)'
        Book b = new Book(isbn: '9780547928227', pageCount: 310)
        b.title = ''

        then: 'now the blank constraint fires'
        !b.validate()
        b.errors.getFieldError('title').code == 'blank'
    }

    void "rejects a title longer than maxSize"() {
        when:
        Book b = new Book(title: 'x' * 256, isbn: '9780547928227', pageCount: 310)

        then:
        !b.validate()
        b.errors.getFieldError('title').code == 'maxSize.exceeded'
    }

    void "rejects a book with a malformed ISBN"() {
        when:
        Book b = new Book(title: 'Test', isbn: 'not-an-isbn', pageCount: 100)

        then:
        !b.validate()
        b.errors.getFieldError('isbn').code == 'matches.invalid'
    }

    void "accepts a book with a valid ISBN-13"() {
        when:
        Book b = new Book(title: 'The Hobbit', isbn: '9780547928227', pageCount: 310)

        then:
        b.validate()
    }

    @Unroll
    void "pageCount #pageCount is #expectedValid"() {
        when:
        Book b = new Book(title: 'X', isbn: '9780547928227', pageCount: pageCount)

        then:
        b.validate() == expectedValid

        where:
        pageCount || expectedValid
        null      || true
        1         || true
        100       || true
        0         || false
        -1        || false
    }
}

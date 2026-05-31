package example

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * DataTest + ServiceUnitTest exercises the @Service(Book) interface
 * against the in-memory datastore. Real GORM finders run; no Spring
 * context, no real database. Best fit for testing query logic
 * (criteria, where queries, finder method names).
 */
class BookServiceSpec extends Specification
        implements ServiceUnitTest<BookService>, DataTest {

    Class[] getDomainClassesToMock() { [Book] }

    void setup() {
        // flush: true forces the in-memory datastore to persist before the
        // GORM finders below query it - without it the finders see zero rows.
        new Book(title: 'Short Story',  isbn: '9780000000001', pageCount: 50 ).save(flush: true, failOnError: true)
        new Book(title: 'Novella',      isbn: '9780000000002', pageCount: 150).save(flush: true, failOnError: true)
        new Book(title: 'Novel',        isbn: '9780000000003', pageCount: 400).save(flush: true, failOnError: true)
        new Book(title: 'Doorstopper',  isbn: '9780000000004', pageCount: 900).save(flush: true, failOnError: true)
    }

    void "countByPageCountGreaterThanEquals returns the right count"() {
        expect:
        service.countByPageCountGreaterThanEquals(200) == 2
        service.countByPageCountGreaterThanEquals(0)   == 4
        service.countByPageCountGreaterThanEquals(901) == 0
    }

    void "findByIsbn returns the matching book"() {
        when:
        Book b = service.findByIsbn('9780000000003')

        then:
        b.title == 'Novel'
    }

    void "findByIsbn returns null when no match"() {
        expect:
        service.findByIsbn('9999999999999') == null
    }

    void "get returns the book when it exists"() {
        given: 'the isbn from the setup fixture'
        Book b = Book.findByIsbn('9780000000001')

        expect:
        service.get(b.id)?.title == 'Short Story'
    }

    void "get returns null when the id does not exist"() {
        expect:
        service.get(99999L) == null
    }

    void "save persists a book and returns it with an id"() {
        when:
        Book saved = service.save(new Book(
            title: 'New Book', isbn: '9780000000005', pageCount: 200))

        then:
        saved.id != null
        saved.title == 'New Book'

        and: 'it is visible to a subsequent query'
        service.countByPageCountGreaterThanEquals(0) == 5
    }

    void "save rejects a duplicate isbn with ValidationException"() {
        given:
        service.save(new Book(title: 'Original', isbn: '9780000000005', pageCount: 100))
        Book.withSession { it.flush() }

        when:
        service.save(new Book(title: 'Duplicate', isbn: '9780000000005', pageCount: 200))

        then:
        def ex = thrown(grails.validation.ValidationException)
        ex.message.contains('unique')
    }

    void "list returns all books when called without constraints"() {
        expect:
        service.list([:]).size() == 4
    }

    void "list respects the max parameter"() {
        expect:
        service.list([max: 2]).size() == 2
    }
}

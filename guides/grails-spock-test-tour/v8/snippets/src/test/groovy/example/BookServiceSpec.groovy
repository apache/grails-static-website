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
}

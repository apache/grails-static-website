package example

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * Unit test for the @Service(Book) data service in shared-core. Both
 * webapps inject this same BookService bean, so testing its query shape
 * once here covers the admin and customer modules' data access.
 */
class BookServiceSpec extends Specification
        implements ServiceUnitTest<BookService>, DataTest {

    Class[] getDomainClassesToMock() { [Book] }

    void setup() {
        new Book(title: 'Old',   isbn: '9780000000001', pageCount: 100, publishedOn: Date.parse('yyyy-MM-dd', '1990-01-01')).save(flush: true, failOnError: true)
        new Book(title: 'Recent', isbn: '9780000000002', pageCount: 200, publishedOn: Date.parse('yyyy-MM-dd', '2020-01-01')).save(flush: true, failOnError: true)
    }

    void "findByIsbn returns the matching book"() {
        expect:
        service.findByIsbn('9780000000002').title == 'Recent'
    }

    void "count reflects persisted books"() {
        expect:
        service.count() == 2
    }

    void "countByPublishedOnGreaterThanEquals filters by date"() {
        expect:
        service.countByPublishedOnGreaterThanEquals(Date.parse('yyyy-MM-dd', '2000-01-01')) == 1
    }
}

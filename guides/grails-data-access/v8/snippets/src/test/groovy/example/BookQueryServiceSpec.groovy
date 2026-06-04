package example

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class BookQueryServiceSpec extends Specification implements ServiceUnitTest<BookQueryService>, DataTest {

    Class[] getDomainClassesToMock() {
        [Author, Book, Tag] as Class[]
    }

    void "findBooksByAuthorName matches case-insensitively"() {
        given:
        Author author = new Author(name: 'Jane Austen', email: 'jane@example.com').save(flush: true, failOnError: true)
        new Book(title: 'Emma', price: 11.50, author: author).save(flush: true, failOnError: true)
        new Book(title: 'Other', price: 5.00, author: new Author(name: 'Other', email: 'o@example.com').save(flush: true)).save(flush: true)

        when:
        List<Book> books = service.findBooksByAuthorName('jane')

        then:
        books*.title == ['Emma']
    }

    void "findBooksWithMinPrice filters and sorts by price descending"() {
        given:
        Author author = new Author(name: 'A', email: 'a@example.com').save(flush: true, failOnError: true)
        new Book(title: 'Low', price: 5.00, author: author).save(flush: true)
        new Book(title: 'High', price: 25.00, author: author).save(flush: true)

        when:
        List<Book> books = service.findBooksWithMinPrice(10.00)

        then:
        books*.title == ['High']
    }
}

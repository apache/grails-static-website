package example

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import java.time.LocalDate

class BookServiceSpec extends Specification implements ServiceUnitTest<BookService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Author, Book, Tag] as Class[]
    }

    void 'listByAuthor uses criteria'() {
        given:
        def ada = new Author(name: 'Ada').save(flush: true)
        def other = new Author(name: 'Other').save(flush: true)
        new Book(title: 'A', price: 10.00, author: ada).save(flush: true)
        new Book(title: 'B', price: 12.00, author: other).save(flush: true)

        expect:
        service.listByAuthor(ada)*.title == ['A']
    }

    void 'findPublishedSince uses where query'() {
        given:
        def author = new Author(name: 'Ada').save(flush: true)
        new Book(title: 'Old', price: 9.99, author: author, publishedOn: LocalDate.of(2000, 1, 1)).save(flush: true)
        new Book(title: 'New', price: 19.99, author: author, publishedOn: LocalDate.of(2024, 6, 1)).save(flush: true)

        expect:
        service.findPublishedSince(LocalDate.of(2020, 1, 1))*.title == ['New']
    }

    void 'searchByTitle uses where query'() {
        given:
        def author = new Author(name: 'Ada').save(flush: true)
        new Book(title: 'Grails Data Access', price: 29.99, author: author).save(flush: true)
        new Book(title: 'Other Topic', price: 14.99, author: author).save(flush: true)

        expect:
        service.searchByTitle('data')*.title == ['Grails Data Access']
    }
}

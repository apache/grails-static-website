package example

import grails.testing.gorm.DataTest
import spock.lang.Specification

import java.time.LocalDate

class BookSpec extends Specification implements DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Author, Book, Tag] as Class[]
    }

    void 'book requires an author'() {
        def book = new Book(title: 'Untitled')

        when:
        boolean valid = book.validate()

        then:
        !valid
        book.errors['author']
    }

    void 'many-to-many tags can be associated'() {
        given:
        def author = new Author(name: 'Test Author').save(flush: true)
        def tag = new Tag(name: 'gorm').save(flush: true)
        def book = new Book(title: 'GORM in Action', price: 39.99, author: author, publishedOn: LocalDate.now()).save(flush: true)

        when:
        book.addToTags(tag)
        book.save(flush: true)

        then:
        Book.get(book.id).tags*.name == ['gorm']
    }
}

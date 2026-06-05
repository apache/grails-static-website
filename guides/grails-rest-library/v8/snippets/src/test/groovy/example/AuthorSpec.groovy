package example

import grails.testing.gorm.DataTest
import spock.lang.Specification

/**
 * Domain constraint unit tests for Author plus the hasMany books mapping.
 */
class AuthorSpec extends Specification implements DataTest {

    Class[] getDomainClassesToMock() { [Author, Book] }

    void "a valid author validates"() {
        expect:
        new Author(name: 'J.R.R. Tolkien').validate()
    }

    void "name is required"() {
        when:
        Author a = new Author()

        then:
        !a.validate()
        a.errors.getFieldError('name').code == 'nullable'
    }

    void "biography is optional but capped at maxSize"() {
        when:
        Author a = new Author(name: 'X', biography: 'a' * 4001)

        then:
        !a.validate()
        a.errors.getFieldError('biography').code == 'maxSize.exceeded'
    }

    void "an author owns many books through addToBooks"() {
        given:
        Author a = new Author(name: 'Tolkien').save(flush: true, failOnError: true)
        a.addToBooks(new Book(title: 'The Hobbit', isbn: '9780547928227', pageCount: 310))
        a.save(flush: true, failOnError: true)

        expect:
        Author.get(a.id).books.size() == 1
        Book.findByIsbn('9780547928227').author.name == 'Tolkien'
    }
}

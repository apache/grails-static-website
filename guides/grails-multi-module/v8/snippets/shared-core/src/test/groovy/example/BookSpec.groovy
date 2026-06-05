package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * Unit test for the Book domain that lives in the shared-core plugin.
 * Proving the domain's constraints here means both webapps inherit a
 * tested model - they never redefine it.
 */
class BookSpec extends Specification implements DomainUnitTest<Book> {

    void "title and isbn are required"() {
        when:
        Book b = new Book()

        then:
        !b.validate()
        b.errors.getFieldError('title').code == 'nullable'
        b.errors.getFieldError('isbn').code == 'nullable'
    }

    void "isbn must match the ISBN pattern and be unique"() {
        given:
        new Book(title: 'First', isbn: '9780547928227', pageCount: 100).save(flush: true, failOnError: true)

        expect:
        !new Book(title: 'Bad', isbn: 'nope').validate()

        and:
        Book dup = new Book(title: 'Dup', isbn: '9780547928227')
        !dup.validate()
        dup.errors.getFieldError('isbn').code == 'unique'
    }

    void "a valid book validates"() {
        expect:
        new Book(title: 'The Hobbit', isbn: '9780547928227', pageCount: 310).validate()
    }
}

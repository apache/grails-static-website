package example

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Domain constraint unit tests for Book. DataTest mocks the associated
 * domains so belongsTo/hasMany are satisfied without a Spring context.
 */
class BookSpec extends Specification implements DataTest {

    Class[] getDomainClassesToMock() { [Author, Book, Tag] }

    private Book bookWith(Map overrides = [:]) {
        Author a = new Author(name: 'A', email: 'a@example.com', bio: 'bio').save(flush: true, failOnError: true)
        new Book([title: 'T', isbn: '9780547928227', genre: 'Fiction', description: 'd',
                  publishedDate: new Date(), priceUSD: 9.99G, inStock: true, author: a] + overrides)
    }

    void "a fully populated book validates"() {
        expect:
        bookWith().validate()
    }

    void "title and description are required"() {
        when:
        Book b = bookWith(title: null, description: null)

        then:
        !b.validate()
        b.errors.getFieldError('title').code == 'nullable'
        b.errors.getFieldError('description').code == 'nullable'
    }

    @Unroll
    void "isbn '#isbn' isValid=#isValid"() {
        expect:
        bookWith(isbn: isbn).validate() == isValid

        where:
        isbn            || isValid
        '9780547928227' || true
        '0547928227'    || true
        'abc'           || false
        '123'           || false
    }

    void "genre must be in the allowed list"() {
        when:
        Book b = bookWith(genre: 'Cooking')

        then:
        !b.validate()
        b.errors.getFieldError('genre').code == 'not.inList'
    }

    void "priceUSD cannot be negative"() {
        when:
        Book b = bookWith(priceUSD: -1.00G)

        then:
        !b.validate()
        b.errors.getFieldError('priceUSD').code == 'min.notmet'
    }
}

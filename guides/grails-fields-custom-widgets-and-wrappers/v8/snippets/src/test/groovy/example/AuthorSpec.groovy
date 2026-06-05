package example

import grails.testing.gorm.DataTest
import spock.lang.Specification

/**
 * Domain constraint unit tests for Author, including the email/url
 * constraints and the hasOne ContactInfo / hasMany Book associations.
 */
class AuthorSpec extends Specification implements DataTest {

    Class[] getDomainClassesToMock() { [Author, Book, Tag, ContactInfo] }

    void "a valid author validates"() {
        expect:
        new Author(name: 'Tolkien', email: 'jrr@example.com', bio: 'bio').validate()
    }

    void "email must be well formed"() {
        when:
        Author a = new Author(name: 'X', email: 'not-an-email', bio: 'bio')

        then:
        !a.validate()
        a.errors.getFieldError('email').code == 'email.invalid'
    }

    void "website must be a valid url when present"() {
        when:
        Author a = new Author(name: 'X', email: 'x@example.com', bio: 'bio', website: 'not a url')

        then:
        !a.validate()
        a.errors.getFieldError('website').code == 'url.invalid'
    }

    void "an author owns many books"() {
        given:
        Author a = new Author(name: 'Tolkien', email: 'jrr@example.com', bio: 'bio')
                .save(flush: true, failOnError: true)
        a.addToBooks(new Book(title: 'The Hobbit', isbn: '9780547928227', genre: 'Fiction',
                description: 'd', publishedDate: new Date(), priceUSD: 9.99G, inStock: true))
        a.save(flush: true, failOnError: true)

        expect:
        Author.get(a.id).books.size() == 1
        // the hasOne ContactInfo association is exercised in LibraryIntegrationSpec,
        // where a real datastore sets the inverse side that DataTest's in-memory
        // store does not.
    }
}

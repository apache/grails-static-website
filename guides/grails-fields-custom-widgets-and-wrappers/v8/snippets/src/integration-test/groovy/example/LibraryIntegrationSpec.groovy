package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * @Integration boots the full context against the in-memory H2 database.
 * @Rollback isolates each method. BootStrap seeds two authors and two books
 * at startup, so these specs assert on that committed seed data and create
 * their own rows with distinct values.
 */
@Integration
@Rollback
class LibraryIntegrationSpec extends Specification {

    void "BootStrap seeded the library"() {
        expect:
        Book.findByTitle('The Hobbit') != null
        Book.findByTitle('Pride and Prejudice') != null
        Author.findByName('J.R.R. Tolkien') != null
    }

    void "a book belongs to an author and has many tags"() {
        given:
        Book hobbit = Book.findByTitle('The Hobbit')

        expect:
        hobbit.author.name == 'J.R.R. Tolkien'
        hobbit.tags*.name.sort() == ['Adventure', 'Classic', 'Fantasy']
    }

    void "an author has a one-to-one contactInfo"() {
        given:
        Author tolkien = Author.findByName('J.R.R. Tolkien')

        expect:
        tolkien.contactInfo != null
        tolkien.contactInfo.phone == '+44 20 7946 0958'
    }

    void "a new book round-trips with its association"() {
        given:
        Author a = Author.findByName('Jane Austen')

        when:
        Book b = new Book(title: 'Emma', isbn: '9780141439587', genre: 'Fiction',
                description: 'A young woman who fancies herself a matchmaker.',
                publishedDate: new Date(), priceUSD: 8.50G, inStock: true, author: a)
                .save(flush: true, failOnError: true)

        then:
        Book.get(b.id).author.name == 'Jane Austen'
    }
}

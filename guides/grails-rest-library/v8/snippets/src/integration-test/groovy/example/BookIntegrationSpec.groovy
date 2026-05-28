package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * @Integration boots the full Spring context against the Testcontainers
 * PostgreSQL database. @Rollback isolates each method, so a bare GORM call
 * runs inside an ambient session.
 *
 * BootStrap seeds four books outside production, so these specs use ISBNs
 * that do not collide with the seed set and filter queries by an author
 * they create themselves.
 */
@Integration
@Rollback
class BookIntegrationSpec extends Specification {

    void "books are filtered by author with a where query"() {
        given:
        Author tolkien = new Author(name: 'Integration Tolkien').save(flush: true, failOnError: true)
        Author leguin  = new Author(name: 'Integration Le Guin').save(flush: true, failOnError: true)
        new Book(author: tolkien, title: 'TT Book', isbn: '9783000000010', pageCount: 310).save(flush: true, failOnError: true)
        new Book(author: leguin,  title: 'LG Book', isbn: '9783000000027', pageCount: 205).save(flush: true, failOnError: true)

        when:
        List<Book> tolkienBooks = Book.where { author.id == tolkien.id }.list()

        then:
        tolkienBooks.size() == 1
        tolkienBooks.first().title == 'TT Book'
    }

    void "pagination returns at most the requested page size"() {
        given:
        Author a = new Author(name: 'Integration Prolific').save(flush: true, failOnError: true)
        (1..5).each { int i ->
            new Book(author: a, title: "Paged ${i}", isbn: String.format('978400000000%d', i), pageCount: 100 + i)
                .save(flush: true, failOnError: true)
        }

        when:
        List<Book> firstPage = Book.where { author.id == a.id }.list(max: 2, offset: 0, sort: 'title', order: 'asc')

        then:
        firstPage.size() == 2
        firstPage*.title == ['Paged 1', 'Paged 2']
    }

    void "an author's books collection reflects persisted books"() {
        given:
        Author a = new Author(name: 'Integration Author').save(flush: true, failOnError: true)
        a.addToBooks(new Book(title: 'One', isbn: '9785000000019', pageCount: 100))
        a.addToBooks(new Book(title: 'Two', isbn: '9785000000026', pageCount: 200))
        a.save(flush: true, failOnError: true)

        expect:
        Author.get(a.id).books.size() == 2
    }
}

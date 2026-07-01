package example

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import java.time.LocalDate

@Integration
class BookDataAccessIntegrationSpec extends Specification {

    BookService bookService
    BookQueryService bookQueryService

    void "criteria, HQL, and where queries run against a real database"() {
        given:
        Author author = Author.withTransaction {
            def existingAuthor = new Author(name: 'Integration Author', email: 'integration@example.com').save(flush: true, failOnError: true)
            new Book(
                title: 'Persistence Patterns',
                price: 29.99,
                author: existingAuthor,
                publishedOn: LocalDate.of(2019, 1, 1)
            ).save(flush: true, failOnError: true)
            new Book(
                title: 'GORM Deep Dive',
                price: 34.50,
                author: existingAuthor,
                publishedOn: LocalDate.of(2024, 6, 1)
            ).save(flush: true, failOnError: true)
            new Book(
                title: 'Grails Data Access',
                price: 19.99,
                author: existingAuthor,
                publishedOn: LocalDate.of(2024, 1, 1)
            ).save(flush: true, failOnError: true)
            existingAuthor
        }

        expect:
        bookQueryService.findBooksByAuthorName('integration').size() == 3
        bookQueryService.countBooksForAuthor(author.id) == 3L
        bookQueryService.findBooksWithMinPrice(30.00)*.title == ['GORM Deep Dive']
        bookService.listByAuthor(author)*.title.sort() == ['GORM Deep Dive', 'Grails Data Access', 'Persistence Patterns']
        bookService.findPublishedSince(LocalDate.of(2020, 1, 1))*.title == ['GORM Deep Dive', 'Grails Data Access']
        bookService.searchByTitle('data')*.title == ['Grails Data Access']
    }
}

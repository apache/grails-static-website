package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * @Integration boots the full Spring context against a real datasource
 * (whatever the `test` environment in application.yml configures - H2
 * by default). @Rollback ensures every test method's database changes
 * are rolled back at the end so specs stay isolated.
 *
 * Use this layer for cross-layer concerns: domain + service + transaction
 * boundary together. Avoid it for what unit tests can answer faster.
 */
@Integration
@Rollback
class BookIntegrationSpec extends Specification {

    BookService bookService

    void "a saved book round-trips through GORM"() {
        given:
        Book draft = new Book(title: 'Round Trip', isbn: '9780547928227', pageCount: 200)

        when:
        Book saved = bookService.save(draft)

        then:
        saved.id != null

        when:
        Book reloaded = bookService.get(saved.id)

        then:
        reloaded.title == 'Round Trip'
        reloaded.isbn  == '9780547928227'
    }

    void "saving a book with a duplicate isbn fails validation"() {
        given:
        bookService.save(new Book(title: 'First',  isbn: '9780547928227', pageCount: 100))

        when:
        Book second = bookService.save(new Book(title: 'Second', isbn: '9780547928227', pageCount: 200))

        then:
        second == null || second.hasErrors()
    }
}

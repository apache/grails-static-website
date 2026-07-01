package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * @Integration boots the full context against the Testcontainers
 * PostgreSQL database. @Rollback isolates each method.
 */
@Integration
@Rollback
class BookIntegrationSpec extends Specification {

    void "a book round-trips through GORM"() {
        given: 'an ISBN distinct from the committed functional-test fixtures'
        Book saved = new Book(title: 'Dune', author: 'Frank Herbert', isbn: '9789999999016')
                .save(flush: true, failOnError: true)

        when:
        Book reloaded = Book.get(saved.id)

        then:
        reloaded.title == 'Dune'
        reloaded.author == 'Frank Herbert'
    }

    void "the unique isbn constraint is enforced at the database layer"() {
        given:
        new Book(title: 'First', author: 'A', isbn: '9789999999023').save(flush: true, failOnError: true)

        when:
        Book dup = new Book(title: 'Second', author: 'B', isbn: '9789999999023')
        dup.save(flush: true)

        then:
        dup.hasErrors()
        Book.countByIsbn('9789999999023') == 1
    }
}

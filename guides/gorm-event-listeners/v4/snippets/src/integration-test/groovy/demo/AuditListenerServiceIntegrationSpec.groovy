package demo

import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Integration
class AuditListenerServiceIntegrationSpec extends Specification {

    BookDataService bookDataService
    AuditDataService auditDataService

    void "saving a Book causes an Audit instance to be saved"() {
        when:
        def conditions = new PollingConditions(timeout: 30)

        Book book = bookDataService.save('Practical Grails 3', 'Eric Helgeson', 1)

        then:
        book
        book.id
        conditions.eventually {
            assert auditDataService.count() == old(auditDataService.count()) + 1
        }

        when:
        Audit lastAudit = this.lastAudit()

        then:
        lastAudit.event == "Book saved"
        lastAudit.bookId == book.id

        when: 'A books is updated'
        book = bookDataService.update(book.id, 'Grails 3')

        then: 'a new audit instance is created'
        conditions.eventually {
            assert auditDataService.count() == old(auditDataService.count()) + 1
        }

        when:
        lastAudit = this.lastAudit()

        then:
        book.title == 'Grails 3'
        lastAudit.event == 'Book updated'
        lastAudit.bookId == book.id

        when: 'A book is deleted'
        bookDataService.delete(book.id)

        then: 'a new audit instance is created'
        conditions.eventually {
            assert auditDataService.count() == old(auditDataService.count()) + 1
        }

        when:
        lastAudit = this.lastAudit()

        then:
        lastAudit.event == 'Book deleted'
        lastAudit.bookId == book.id

        cleanup:
        auditDataService.deleteByBookId(book.id)
    }

    Audit lastAudit() {
        int offset = Math.max(((auditDataService.count() as int) - 1), 0)
        auditDataService.findAll([max: 1, offset: offset]).first()
    }
}

package demo

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class TitleListenerServiceIntegrationSpec extends Specification {

    BookDataService bookDataService
    AuditDataService auditDataService

    def "saving a book, generates automatically a serial number"() {
        when:
        Book book = bookDataService.save('Practical Grails 3', 'Eric Helgeson', 100)
        String serialNumber = book.serialNumber
        String friendlyUrl = book.friendlyUrl

        then:
        book
        !book.hasErrors()
        serialNumber
        friendlyUrl == 'practical-grails-3'

        when: 'updating book title'
        book = bookDataService.update(book.id, 'Grails 3')

        then: 'serial number stays the same'
        serialNumber == book.serialNumber

        and: 'friendly url changes'
        friendlyUrl != book.friendlyUrl
        book.friendlyUrl == 'grails-3'

        cleanup:
        auditDataService.deleteByBookId(book.id)
        bookDataService.delete(book.id)
    }
}

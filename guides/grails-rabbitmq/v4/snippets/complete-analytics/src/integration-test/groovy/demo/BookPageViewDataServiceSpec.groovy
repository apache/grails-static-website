package demo

import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf( { System.getenv('TRAVIS') as boolean } )
@Integration
class BookPageViewDataServiceSpec extends Specification {

    BookPageViewDataService bookPageViewDataService

    def "test increments"() {
        expect:
        !bookPageViewDataService.findByBookId(2)

        when:
        bookPageViewDataService.increment(2, 'Practical Grails 3')
        BookPageView bookPageView = bookPageViewDataService.findByBookId(2)

        then:
        bookPageView
        bookPageViewDataService.findByBookId(2).views == 1

        when:
        bookPageViewDataService.increment(2, 'Practical Grails 3')
        bookPageView = bookPageViewDataService.findByBookId(2)

        then:
        bookPageView
        bookPageViewDataService.findByBookId(2).views == 2

        cleanup:
        bookPageViewDataService.delete(bookPageView.id)
    }
}

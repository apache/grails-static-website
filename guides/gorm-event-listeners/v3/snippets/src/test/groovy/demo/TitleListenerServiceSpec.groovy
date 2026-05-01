package demo

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

class TitleListenerServiceSpec extends Specification implements ServiceUnitTest<TitleListenerService>, DataTest {

    def setupSpec() {
        mockDomain Book
    }

    Closure doWithSpring() {{ -> // <1>
        friendlyUrlService(FriendlyUrlService)
    }}

    @Rollback
    void "test serial number generated"() {
        given:
        Book book = new Book(title: 'Practical Grails 3', author: 'Eric Helgeson', pages: 100)

        when:
        service.serialNumberGeneratorService = Stub(SerialNumberGeneratorService) {
            generate(_ as String) >> 'XXXX-5125'
        }

        service.onBookPreInsert(new PreInsertEvent(dataStore, book))

        then:
        book.serialNumber == 'XXXX-5125'
        book.friendlyUrl == 'practical-grails-3'
    }
}

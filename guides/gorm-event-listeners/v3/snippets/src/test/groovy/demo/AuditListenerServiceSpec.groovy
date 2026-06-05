package demo

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import spock.lang.Specification

class AuditListenerServiceSpec extends Specification implements ServiceUnitTest<AuditListenerService>, DataTest { //<1>

    void setupSpec() {
        mockDomains Book //<2>
    }

    void "Book.PostInsertEvent triggers auditDataService.save"(){
        given:
        service.auditDataService = Mock(AuditDataService)

        Book book = new Book(title: 'Practical Grails 3',
                author: 'Eric Helgeson',
                pages: 1).save() //<3>
        PostInsertEvent event = new PostInsertEvent(dataStore, book) //<4>

        when:
        service.afterInsert(event) //<5>

        then:
        1 * service.auditDataService.save(_, _) //<6>
    }

    void "Book.PostUpdateEvent triggers auditDataService.save"(){
        given:
        service.auditDataService = Mock(AuditDataService)

        Book book = new Book(title: 'Practical Grails 3',
                author: 'Eric Helgeson',
                pages: 1).save() //<3>
        PostUpdateEvent event = new PostUpdateEvent(dataStore, book) //<4>

        when:
        service.afterUpdate(event) //<5>

        then:
        1 * service.auditDataService.save(_, _) //<6>
    }

    void "Book.PostDeleteEvent triggers auditDataService.save"(){
        given:
        service.auditDataService = Mock(AuditDataService)

        Book book = new Book(title: 'Practical Grails 3',
                author: 'Eric Helgeson',
                pages: 1).save() //<3>
        PostDeleteEvent event = new PostDeleteEvent(dataStore, book) //<4>

        when:
        service.afterDelete(event) //<5>

        then:
        1 * service.auditDataService.save(_, _) //<6>
    }
}

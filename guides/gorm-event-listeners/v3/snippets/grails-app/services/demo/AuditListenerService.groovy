package demo

import grails.events.annotation.Subscriber
import grails.events.annotation.gorm.Listener
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent

@Slf4j
@CompileStatic
class AuditListenerService {

    AuditDataService auditDataService

    Long bookId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof Book ) {
            return ((Book) event.entityObject).id // <2>
        }
        null
    }

    @Subscriber // <1>
    void afterInsert(PostInsertEvent event) {
        Long bookId = bookId(event)
        if ( bookId ) {
            log.info 'After book save...'
            auditDataService.save('Book saved', bookId)
        }
    }

    @Subscriber // <1>
    void afterUpdate(PostUpdateEvent event) { // <3>
        Long bookId = bookId(event)
        if ( bookId ) {
            log.info "After book update..."
            auditDataService.save('Book updated', bookId)
        }
    }

    @Subscriber // <1>
    void afterDelete(PostDeleteEvent event) {
        Long bookId = bookId(event)
        if ( bookId ) {
            log.info 'After book delete ...'
            auditDataService.save('Book deleted', bookId)
        }
    }
}

package demo

import grails.events.annotation.gorm.Listener
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

@CompileStatic
class TitleListenerService {

    FriendlyUrlService friendlyUrlService
    SerialNumberGeneratorService serialNumberGeneratorService

    @Listener(Book) // <1>
    void onBookPreInsert(PreInsertEvent event) {
        populateSerialNumber(event)
        populateFriendlyUrl(event)
    }

    @Listener(Book) // <1>
    void onBookPreUpdate(PreUpdateEvent event) { // <2>
        Book book = ((Book) event.entityObject)
        if ( book.isDirty('title') ) { // <3>
            populateFriendlyUrl(event)
        }
    }

    void populateSerialNumber(AbstractPersistenceEvent event) {
        String title = event.entityAccess.getProperty('title') as String // <4>
        String serialNumber = serialNumberGeneratorService.generate(title)
        event.entityAccess.setProperty('serialNumber', serialNumber) //<5>
    }

    void populateFriendlyUrl(AbstractPersistenceEvent event) {
        String title = event.entityAccess.getProperty('title') as String
        String friendlyUrl = friendlyUrlService.sanitizeWithDashes(title)
        event.entityAccess.setProperty('friendlyUrl', friendlyUrl)
    }
}
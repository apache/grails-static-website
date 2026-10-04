package example.grails

import groovy.transform.CompileStatic

@CompileStatic
class DeliveryCreatedEvent {
    final Long deliveryId

    DeliveryCreatedEvent(Long deliveryId) {
        this.deliveryId = deliveryId
    }
}

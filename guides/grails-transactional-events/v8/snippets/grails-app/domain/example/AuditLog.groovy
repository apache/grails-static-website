package example

import grails.persistence.Entity

@Entity
class AuditLog {

    String eventType
    Long orderId
    Long customerId
    BigDecimal orderTotal
    Date occurredAt

    static constraints = {
        eventType  blank: false, maxSize: 64
        orderId    min: 1L
        customerId min: 1L
        orderTotal min: new BigDecimal('0.01')
        occurredAt nullable: false
    }

    static mapping = {
        orderTotal scale: 2, precision: 19
    }
}

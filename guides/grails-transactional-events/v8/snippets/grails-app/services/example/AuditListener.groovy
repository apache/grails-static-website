package example

import example.events.OrderPlacedEvent
import grails.gorm.transactions.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Persists an AuditLog row for every committed order.
 *
 * A separate listener class - not a method on OrderService - keeps the
 * "place an order" concern decoupled from the "record audit history"
 * concern. Adding another committed-only side-effect later means a new
 * class, not an edit to OrderService.
 */
class AuditListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void onOrderPlaced(OrderPlacedEvent event) {
        new AuditLog(
                eventType:  'ORDER_PLACED',
                orderId:    event.orderId,
                customerId: event.customerId,
                orderTotal: event.total,
                occurredAt: new Date()
        ).save(failOnError: true)
    }
}

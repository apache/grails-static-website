package example

import example.events.OrderPlacedEvent
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Persists an AuditLog row for every committed order.
 *
 * A separate listener class - not a method on OrderService - keeps the
 * "place an order" concern decoupled from the "record audit history"
 * concern. Adding another committed-only side-effect later means a new
 * class, not an edit to OrderService.
 *
 * Like CustomerLifetimeValueListener, it is registered in resources.groovy
 * and does its GORM write inside withNewTransaction { } because the
 * AFTER_COMMIT callback fires after the publisher's transaction has committed.
 */
class AuditListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onOrderPlaced(OrderPlacedEvent event) {
        AuditLog.withNewTransaction {
            new AuditLog(
                    eventType:  'ORDER_PLACED',
                    orderId:    event.orderId,
                    customerId: event.customerId,
                    orderTotal: event.total,
                    occurredAt: new Date()
            ).save(failOnError: true)
        }
    }
}

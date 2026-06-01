package example

import example.events.OrderPlacedEvent
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Persists an AuditLog row for every committed order.
 *
 * A separate service - not a method on OrderService - keeps the "place an
 * order" concern decoupled from the "record audit history" concern. Adding
 * another committed-only side-effect later means a new service, not an edit to
 * OrderService.
 *
 * Like CustomerLifetimeValueService it is auto-registered by Grails (its name
 * ends in 'Service') and does its GORM write inside withNewTransaction { }
 * because the AFTER_COMMIT callback fires after the publisher's transaction
 * has committed.
 */
class AuditService {

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

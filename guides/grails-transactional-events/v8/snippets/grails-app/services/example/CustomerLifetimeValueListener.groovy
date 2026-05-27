package example

import example.events.OrderPlacedEvent
import grails.gorm.transactions.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Updates Customer.lifetimeValue when an order commits.
 *
 * Grails auto-registers everything in grails-app/services/ as a Spring bean,
 * so @TransactionalEventListener annotations on methods here are picked up
 * by Spring's EventListenerMethodProcessor at startup.
 */
class CustomerLifetimeValueListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void onOrderPlaced(OrderPlacedEvent event) {
        Customer customer = Customer.get(event.customerId)
        if (customer) {
            customer.lifetimeValue = (customer.lifetimeValue ?: BigDecimal.ZERO) + event.total
            customer.save(flush: true, failOnError: true)
        }
    }
}

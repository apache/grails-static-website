package example

import example.events.OrderPlacedEvent
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Updates Customer.lifetimeValue after an order commits.
 *
 * An ordinary Grails service: because the class name ends in 'Service', Grails
 * auto-registers it as a Spring bean, and Spring scans its
 * @TransactionalEventListener method - no manual wiring. It carries no
 * @Transactional of its own, so Grails applies no transactional proxy.
 *
 * The AFTER_COMMIT callback runs after the publisher's transaction has already
 * committed, so this method opens its OWN transaction for the GORM write via
 * withNewTransaction { }. Do not reach for @Transactional(REQUIRES_NEW) to make
 * that declarative: Grails' @Transactional is an AST transform that relocates
 * the method body and would hide @TransactionalEventListener from Spring's
 * scanner, so the listener would never fire.
 */
class CustomerLifetimeValueService {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onOrderPlaced(OrderPlacedEvent event) {
        Customer.withNewTransaction {
            Customer customer = Customer.get(event.customerId)
            if (customer) {
                customer.lifetimeValue = (customer.lifetimeValue ?: BigDecimal.ZERO) + event.total
                customer.save(flush: true, failOnError: true)
            }
        }
    }
}

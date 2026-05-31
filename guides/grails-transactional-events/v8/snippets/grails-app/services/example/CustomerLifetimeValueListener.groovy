package example

import example.events.OrderPlacedEvent
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Updates Customer.lifetimeValue after an order commits.
 *
 * Registered as a Spring bean in grails-app/conf/spring/resources.groovy -
 * Grails only auto-registers grails-app/services/ classes whose name ends in
 * 'Service', and this one ends in 'Listener'. That registration (plus the
 * TransactionalEventListenerFactory bean resources.groovy also declares) is
 * what lets Spring wire the @TransactionalEventListener method.
 *
 * The AFTER_COMMIT callback runs after the publisher's transaction has
 * already committed, so this method opens its OWN transaction for the GORM
 * write via withNewTransaction { }. An @Transactional(REQUIRES_NEW)
 * annotation does NOT work here: a bean registered in resources.groovy is not
 * wrapped in Grails' transactional proxy, so the annotation is ignored and
 * the save fails with "no transaction is in progress".
 */
class CustomerLifetimeValueListener {

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

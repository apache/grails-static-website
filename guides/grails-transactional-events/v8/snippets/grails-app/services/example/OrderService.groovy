package example

import example.events.OrderPlacedEvent
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher

@Transactional
class OrderService {

    @Autowired
    ApplicationEventPublisher applicationEventPublisher

    Order placeOrder(Long customerId, BigDecimal total) {
        Customer customer = Customer.get(customerId)
        if (!customer) {
            throw new IllegalArgumentException("Unknown customer: ${customerId}")
        }

        Order order = new Order(customer: customer, total: total).save(failOnError: true)

        // Fire-and-forget from the publisher's point of view. Spring buffers
        // the event in the synchronization manager and dispatches it to
        // @TransactionalEventListener beans only after this method's
        // transaction commits.
        applicationEventPublisher.publishEvent(
                new OrderPlacedEvent(order.id, customer.id, total))

        order
    }
}

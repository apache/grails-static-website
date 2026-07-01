package example.events

import groovy.transform.Immutable

/**
 * Domain event published by OrderService after an Order is persisted.
 *
 * No ApplicationEvent inheritance is required. Spring's
 * ApplicationEventPublisher.publishEvent(Object) overload wraps any
 * payload in a PayloadApplicationEvent transparently, so plain POGOs
 * are the recommended event type since Spring Framework 4.2.
 */
@Immutable
class OrderPlacedEvent {
    Long orderId
    Long customerId
    BigDecimal total
}

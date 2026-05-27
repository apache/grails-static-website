package example

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * Integration spec that proves the two non-negotiable claims:
 *
 *   1. AFTER_COMMIT listeners fire when (and only when) the outer
 *      transaction commits.
 *   2. AFTER_COMMIT listeners are silently skipped when the outer
 *      transaction rolls back - no phantom audit rows, no phantom
 *      emails for orders that never durably existed.
 *
 * Each test method opens its own committed transaction via
 * Customer.withNewTransaction { ... } / Order.withNewTransaction { ... }
 * because @Rollback would also prevent the AFTER_COMMIT listeners from
 * firing, which would conflate "the listener never ran because rollback"
 * with "the listener never ran because @Rollback".
 */
@Integration
class OrderServiceIntegrationSpec extends Specification {

    OrderService orderService

    void "AFTER_COMMIT listeners run only after the outer transaction commits"() {
        given:
        Customer customer = createCommitted('Eve Lin', 'eve@example.com')
        long auditBefore = AuditLog.count()
        BigDecimal lvBefore = Customer.get(customer.id).lifetimeValue

        when: 'placeOrder runs inside a transaction that commits'
        Order placed = null
        Order.withNewTransaction {
            placed = orderService.placeOrder(customer.id, new BigDecimal('19.95'))
        }

        then: 'Both AFTER_COMMIT listeners fired'
        AuditLog.count() == auditBefore + 1
        Customer.get(customer.id).lifetimeValue == lvBefore + new BigDecimal('19.95')

        cleanup:
        deleteCommitted(placed, customer)
    }

    void "AFTER_COMMIT listeners are skipped when the outer transaction rolls back"() {
        given:
        Customer customer = createCommitted('Fay Park', 'fay@example.com')
        long auditBefore = AuditLog.count()
        BigDecimal lvBefore = Customer.get(customer.id).lifetimeValue

        when: 'placeOrder runs inside a transaction that explicitly rolls back'
        Order.withNewTransaction { status ->
            orderService.placeOrder(customer.id, new BigDecimal('999.00'))
            status.setRollbackOnly()
        }

        then: 'Neither AFTER_COMMIT listener fired'
        AuditLog.count() == auditBefore
        Customer.get(customer.id).lifetimeValue == lvBefore

        cleanup:
        deleteCommitted(null, customer)
    }

    private Customer createCommitted(String name, String email) {
        Customer created = null
        Customer.withNewTransaction {
            created = new Customer(name: name, email: email).save(flush: true, failOnError: true)
        }
        created
    }

    private void deleteCommitted(Order placed, Customer customer) {
        Order.withNewTransaction {
            if (placed) {
                AuditLog.where { orderId == placed.id }.deleteAll()
                Order.get(placed.id)?.delete(flush: true)
            }
            Customer.get(customer.id)?.delete(flush: true)
        }
    }
}

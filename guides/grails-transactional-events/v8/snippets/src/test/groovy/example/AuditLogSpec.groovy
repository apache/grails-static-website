package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AuditLogSpec extends Specification implements DomainUnitTest<AuditLog> {

    private void populate() {
        domain.eventType = 'ORDER_PLACED'
        domain.orderId = 1L
        domain.customerId = 1L
        domain.orderTotal = new BigDecimal('19.95')
        domain.occurredAt = new Date()
    }

    void "a fully populated audit row validates"() {
        when:
        populate()

        then:
        domain.validate()
    }

    void "a blank eventType is rejected"() {
        when:
        populate()
        domain.eventType = ''

        then:
        !domain.validate()
        domain.errors['eventType'].code == 'blank'
    }

    void "occurredAt is required"() {
        when:
        populate()
        domain.occurredAt = null

        then:
        !domain.validate()
        domain.errors['occurredAt'].code == 'nullable'
    }

    void "an orderTotal below the 0.01 minimum is rejected"() {
        when:
        populate()
        domain.orderTotal = BigDecimal.ZERO

        then:
        !domain.validate()
        domain.errors['orderTotal'].code == 'min.notmet'
    }
}

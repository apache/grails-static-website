package example

import grails.testing.gorm.DataTest
import spock.lang.Specification

class OrderSpec extends Specification implements DataTest {

    Class[] getDomainClassesToMock() {
        [Order, Customer] as Class[]
    }

    private Customer validCustomer() {
        new Customer(name: 'Alice Wong', email: 'alice@example.com').save(flush: true)
    }

    void "an order with a customer and a positive total validates"() {
        when:
        Order order = new Order(customer: validCustomer(), total: new BigDecimal('19.95'))

        then:
        order.validate()
    }

    void "an order requires a customer"() {
        when:
        Order order = new Order(total: new BigDecimal('19.95'))

        then:
        !order.validate()
        order.errors['customer'].code == 'nullable'
    }

    void "a zero total is rejected (min is 0.01)"() {
        when:
        Order order = new Order(customer: validCustomer(), total: BigDecimal.ZERO)

        then:
        !order.validate()
        order.errors['total'].code == 'min.notmet'
    }
}

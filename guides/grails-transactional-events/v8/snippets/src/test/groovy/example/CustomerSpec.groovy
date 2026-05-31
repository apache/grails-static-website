package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class CustomerSpec extends Specification implements DomainUnitTest<Customer> {

    void "a customer with a name and a valid email validates"() {
        when:
        domain.name = 'Alice Wong'
        domain.email = 'alice@example.com'

        then:
        domain.validate()
        domain.lifetimeValue == BigDecimal.ZERO
    }

    void "a blank name is rejected"() {
        when:
        domain.name = ''
        domain.email = 'alice@example.com'

        then:
        !domain.validate()
        domain.errors['name'].code == 'blank'
    }

    void "a malformed email is rejected"() {
        when:
        domain.name = 'Alice Wong'
        domain.email = 'not-an-email'

        then:
        !domain.validate()
        domain.errors['email'].code == 'email.invalid'
    }

    void "a negative lifetimeValue is rejected"() {
        when:
        domain.name = 'Alice Wong'
        domain.email = 'alice@example.com'
        domain.lifetimeValue = new BigDecimal('-0.01')

        then:
        !domain.validate()
        domain.errors['lifetimeValue'].code == 'min.notmet'
    }

    void "email must be unique"() {
        given:
        new Customer(name: 'First', email: 'dup@example.com').save(flush: true)

        when:
        Customer second = new Customer(name: 'Second', email: 'dup@example.com')

        then:
        !second.validate()
        second.errors['email'].code == 'unique'
    }
}

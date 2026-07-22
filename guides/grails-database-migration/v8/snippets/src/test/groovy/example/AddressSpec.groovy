package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AddressSpec extends Specification implements DomainUnitTest<Address> {

    void 'person is required'() {
        when:
        def address = new Address()

        then:
        !address.validate()
        address.errors['person'].code == 'nullable'
    }

    void 'address fields are optional when person is set'() {
        given:
        mockDomain(Person)

        when:
        def person = new Person(name: 'Ada', age: 36).save(flush: true)
        def address = new Address(person: person)

        then:
        address.validate()
    }
}

package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AddressSpec extends Specification implements DomainUnitTest<Address> {

    void 'address fields are optional'() {
        when:
        def address = new Address()

        then:
        address.validate()
    }
}

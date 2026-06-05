package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class CustomerRequestSpec extends Specification implements DomainUnitTest<CustomerRequest> {

    void "a request defaults to SUBMITTED and validates with a summary"() {
        when:
        domain.summary = 'Replace the rooftop HVAC unit'

        then:
        domain.validate()
        domain.status == CustomerRequestStatus.SUBMITTED
    }

    void "a blank summary is rejected"() {
        when:
        domain.summary = ''

        then:
        !domain.validate()
        domain.errors['summary'].code == 'blank'
    }

    void "status cannot be null"() {
        when:
        domain.summary = 'A request'
        domain.status = null

        then:
        !domain.validate()
        domain.errors['status'].code == 'nullable'
    }
}

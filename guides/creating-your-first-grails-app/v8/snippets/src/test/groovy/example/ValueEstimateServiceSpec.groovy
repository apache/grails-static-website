package example

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class ValueEstimateServiceSpec extends Specification
        implements ServiceUnitTest<ValueEstimateService>, DataTest {

    void setupSpec() {
        mockDomains Make, Model, Vehicle
    }

    void 'estimate is a sensible positive number'() {
        given:
        def make = new Make(name: 'Test')
        def model = new Model(name: 'Test', make: make)
        def vehicle = new Vehicle(year: 2000, make: make, model: model, name: 'Test Vehicle')

        when:
        def estimate = service.getEstimate(vehicle)

        then:
        estimate > 0
        estimate < 1_000_000
    }
}

package example

import grails.testing.gorm.DataTest
import spock.lang.Specification

class WorkOrderSpec extends Specification implements DataTest {

    Class[] getDomainClassesToMock() {
        [WorkOrder, CustomerRequest] as Class[]
    }

    private CustomerRequest validRequest() {
        new CustomerRequest(summary: 'Replace the rooftop HVAC unit').save(flush: true)
    }

    void "a work order defaults to OPEN and validates with a request and description"() {
        when:
        WorkOrder workOrder = new WorkOrder(
                customerRequest: validRequest(), description: 'Site survey and install')

        then:
        workOrder.validate()
        workOrder.status == WorkOrderStatus.OPEN
    }

    void "a work order requires a customer request"() {
        when:
        WorkOrder workOrder = new WorkOrder(description: 'Site survey and install')

        then:
        !workOrder.validate()
        workOrder.errors['customerRequest'].code == 'nullable'
    }

    void "a blank description is rejected"() {
        given: 'a work order whose description is set blank directly (bypassing map-constructor data binding, which would convert "" to null)'
        WorkOrder workOrder = new WorkOrder(customerRequest: validRequest(), description: 'placeholder')

        when:
        workOrder.description = ''

        then:
        !workOrder.validate()
        workOrder.errors['description'].code == 'blank'
    }

    void "status cannot be null"() {
        when:
        WorkOrder workOrder = new WorkOrder(
                customerRequest: validRequest(), description: 'Work', status: null)

        then:
        !workOrder.validate()
        workOrder.errors['status'].code == 'nullable'
    }
}

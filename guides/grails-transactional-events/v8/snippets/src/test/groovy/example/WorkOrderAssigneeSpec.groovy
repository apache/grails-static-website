package example

import grails.testing.gorm.DataTest
import spock.lang.Specification

class WorkOrderAssigneeSpec extends Specification implements DataTest {

    Class[] getDomainClassesToMock() {
        [WorkOrderAssignee, WorkOrder, CustomerRequest] as Class[]
    }

    private WorkOrder validWorkOrder() {
        CustomerRequest request = new CustomerRequest(
                summary: 'Replace the rooftop HVAC unit').save(flush: true)
        new WorkOrder(customerRequest: request, description: 'Site survey and install')
                .save(flush: true)
    }

    void "an assignee with a work order and an employee name validates"() {
        when:
        WorkOrderAssignee assignee = new WorkOrderAssignee(
                workOrder: validWorkOrder(), employeeName: 'Dana Reed')

        then:
        assignee.validate()
    }

    void "an assignee requires a work order"() {
        when:
        WorkOrderAssignee assignee = new WorkOrderAssignee(employeeName: 'Dana Reed')

        then:
        !assignee.validate()
        assignee.errors['workOrder'].code == 'nullable'
    }

    void "a blank employee name is rejected"() {
        given: 'employeeName set blank directly (the map constructor would convert "" to null via data binding)'
        WorkOrderAssignee assignee = new WorkOrderAssignee(
                workOrder: validWorkOrder(), employeeName: 'placeholder')

        when:
        assignee.employeeName = ''

        then:
        !assignee.validate()
        assignee.errors['employeeName'].code == 'blank'
    }
}

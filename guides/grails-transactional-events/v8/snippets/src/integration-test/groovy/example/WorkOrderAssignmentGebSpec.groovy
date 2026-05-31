package example

import example.pages.WorkOrderPage
import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

/**
 * Browser-driven end-to-end test of the synchronous orchestration. A real
 * Selenium-Chrome browser (started in a Testcontainers container by
 * ContainerGebSpec) loads the work-order page, submits the assignment form,
 * and asserts the cascaded statuses the user actually sees - proving the
 * pattern works through the entire stack from the browser down to the DB.
 */
@Integration
class WorkOrderAssignmentGebSpec extends ContainerGebSpec {

    void "assigning an employee through the UI plans the work order and advances the request"() {
        given:
        Long[] ids = createScenario(CustomerRequestStatus.SUBMITTED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]

        when: 'the work-order page is opened'
        to WorkOrderPage, workOrderId: workOrderId

        then: 'it shows the pre-assignment state'
        workOrderStatus == 'OPEN'
        requestStatus == 'SUBMITTED'

        when: 'an employee is assigned through the form'
        assignEmployee('Dana Reed')

        then: 'the page reflects the committed cascade'
        at WorkOrderPage
        workOrderStatus == 'PLANNED'
        requestStatus == 'IN_PROGRESS'

        cleanup:
        deleteScenario(workOrderId, requestId)
    }

    void "the UI surfaces a conflict and leaves every status unchanged for a closed request"() {
        given:
        Long[] ids = createScenario(CustomerRequestStatus.CANCELLED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]

        when: 'an employee is assigned to a work order whose request is cancelled'
        to WorkOrderPage, workOrderId: workOrderId
        assignEmployee('Dana Reed')

        then: 'the conflict message shows and the statuses are unchanged'
        at WorkOrderPage
        message.text().contains('Cannot start work')
        workOrderStatus == 'OPEN'
        requestStatus == 'CANCELLED'

        and: 'nothing persisted'
        countAssignees(workOrderId) == 0

        cleanup:
        deleteScenario(workOrderId, requestId)
    }

    private Long[] createScenario(CustomerRequestStatus requestStatus) {
        Long requestId = null
        Long workOrderId = null
        CustomerRequest.withNewTransaction {
            CustomerRequest request = new CustomerRequest(
                    summary: 'Replace the rooftop HVAC unit', status: requestStatus)
                    .save(flush: true, failOnError: true)
            WorkOrder workOrder = new WorkOrder(
                    customerRequest: request, description: 'Site survey and install')
                    .save(flush: true, failOnError: true)
            requestId = request.id
            workOrderId = workOrder.id
        }
        [requestId, workOrderId] as Long[]
    }

    private long countAssignees(Long workOrderId) {
        WorkOrderAssignee.withNewTransaction {
            WorkOrderAssignee.where { workOrder.id == workOrderId }.count()
        }
    }

    private void deleteScenario(Long workOrderId, Long requestId) {
        WorkOrder.withNewTransaction {
            WorkOrder workOrder = WorkOrder.get(workOrderId)
            if (workOrder) {
                WorkOrderAssignee.findAllByWorkOrder(workOrder)*.delete()
                workOrder.delete(flush: true)
            }
            CustomerRequest.get(requestId)?.delete(flush: true)
        }
    }
}

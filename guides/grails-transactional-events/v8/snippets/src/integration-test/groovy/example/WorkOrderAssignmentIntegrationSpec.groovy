package example

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * Integration spec that proves the same-transaction orchestration is atomic:
 *
 *   1. On success, the assignment, the WorkOrder status, and the
 *      CustomerRequest status all commit together.
 *   2. On a failure raised mid-orchestration (the listener rejecting an
 *      illegal request transition), EVERY change rolls back - no assignee
 *      row, the WorkOrder is still OPEN, the CustomerRequest is untouched.
 *
 * As in OrderServiceIntegrationSpec, each method drives its own committed
 * transactions via withNewTransaction { ... } and asserts in fresh
 * transactions, so the assertions read committed database state rather than
 * mutated first-level-cache instances.
 */
@Integration
class WorkOrderAssignmentIntegrationSpec extends Specification {

    WorkOrderAssignmentService workOrderAssignmentService

    void "assigning an employee atomically plans the work order and advances the request"() {
        given:
        Long[] ids = createScenario(CustomerRequestStatus.SUBMITTED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]

        when: 'an employee is assigned inside a transaction that commits'
        WorkOrderAssignee assignee = null
        WorkOrder.withNewTransaction {
            assignee = workOrderAssignmentService.assignEmployee(workOrderId, 'Dana Reed')
        }

        then: 'all three changes committed as one unit'
        assignee.id != null
        WorkOrder.withNewTransaction { WorkOrder.get(workOrderId).status } == WorkOrderStatus.PLANNED
        CustomerRequest.withNewTransaction { CustomerRequest.get(requestId).status } == CustomerRequestStatus.IN_PROGRESS

        cleanup:
        deleteScenario(workOrderId, requestId)
    }

    void "a failure mid-orchestration rolls every change back"() {
        given: 'a work order whose customer request is already closed'
        Long[] ids = createScenario(CustomerRequestStatus.COMPLETED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]
        long assigneesBefore = countAssignees(workOrderId)

        when: 'assigning an employee triggers an illegal request transition'
        WorkOrder.withNewTransaction {
            workOrderAssignmentService.assignEmployee(workOrderId, 'Dana Reed')
        }

        then: 'the orchestration threw'
        thrown(IllegalStateException)

        and: 'nothing persisted - no assignee, both statuses unchanged'
        countAssignees(workOrderId) == assigneesBefore
        WorkOrder.withNewTransaction { WorkOrder.get(workOrderId).status } == WorkOrderStatus.OPEN
        CustomerRequest.withNewTransaction { CustomerRequest.get(requestId).status } == CustomerRequestStatus.COMPLETED

        cleanup:
        deleteScenario(workOrderId, requestId)
    }

    void "assigning a second employee to an already-planned work order does not re-run the cascade"() {
        given: 'a work order already planned by a first assignment'
        Long[] ids = createScenario(CustomerRequestStatus.SUBMITTED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]
        WorkOrder.withNewTransaction {
            workOrderAssignmentService.assignEmployee(workOrderId, 'Dana Reed')
        }

        when: 'a second employee is assigned'
        WorkOrder.withNewTransaction {
            workOrderAssignmentService.assignEmployee(workOrderId, 'Sam Ortiz')
        }

        then: 'both assignees exist, but the OPEN/SUBMITTED guards make the status changes a no-op'
        countAssignees(workOrderId) == 2
        WorkOrder.withNewTransaction { WorkOrder.get(workOrderId).status } == WorkOrderStatus.PLANNED
        CustomerRequest.withNewTransaction { CustomerRequest.get(requestId).status } == CustomerRequestStatus.IN_PROGRESS

        cleanup:
        deleteScenario(workOrderId, requestId)
    }

    void "a cancelled request also blocks the assignment and rolls everything back"() {
        given:
        Long[] ids = createScenario(CustomerRequestStatus.CANCELLED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]
        long assigneesBefore = countAssignees(workOrderId)

        when:
        WorkOrder.withNewTransaction {
            workOrderAssignmentService.assignEmployee(workOrderId, 'Dana Reed')
        }

        then:
        thrown(IllegalStateException)

        and:
        countAssignees(workOrderId) == assigneesBefore
        WorkOrder.withNewTransaction { WorkOrder.get(workOrderId).status } == WorkOrderStatus.OPEN
        CustomerRequest.withNewTransaction { CustomerRequest.get(requestId).status } == CustomerRequestStatus.CANCELLED

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

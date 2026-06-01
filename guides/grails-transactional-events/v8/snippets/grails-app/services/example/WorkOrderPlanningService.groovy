package example

import example.events.EmployeeAssignedEvent
import org.springframework.context.event.EventListener

/**
 * Synchronous, in-transaction orchestration.
 *
 * A plain @EventListener (NOT @TransactionalEventListener) runs inline on the
 * publisher's thread, inside the publisher's transaction and Hibernate session:
 * reads see in-flight state, writes join the same commit, and a thrown
 * exception rolls the whole operation back.
 *
 * It is an ordinary Grails service (auto-registered by name) but deliberately
 * carries NO @Transactional: Grails' @Transactional is a compile-time AST
 * transform that relocates the method body and would hide @EventListener from
 * Spring's scanner, so the listener would never register. It needs no
 * transaction of its own anyway - it runs inside the one the publisher started.
 */
class WorkOrderPlanningService {

    @EventListener
    void onEmployeeAssigned(EmployeeAssignedEvent event) {
        WorkOrder workOrder = WorkOrder.get(event.workOrderId)

        // An assigned work order is ready to be planned.
        if (workOrder.status == WorkOrderStatus.OPEN) {
            workOrder.status = WorkOrderStatus.PLANNED
            workOrder.save(failOnError: true)
        }

        // Planning work means the originating request is now in progress.
        // This read sees the WorkOrder transition made one statement ago,
        // because both run in the same transaction.
        CustomerRequest request = workOrder.customerRequest
        if (request.status.terminal) {
            throw new IllegalStateException(
                    "Cannot start work on request ${request.id}: it is ${request.status}")
        }
        if (request.status == CustomerRequestStatus.SUBMITTED) {
            request.status = CustomerRequestStatus.IN_PROGRESS
            request.save(failOnError: true)
        }
    }
}

package example

import example.events.EmployeeAssignedEvent
import org.springframework.context.event.EventListener

/**
 * Synchronous, in-transaction orchestration.
 *
 * The annotation is a plain @EventListener - NOT @TransactionalEventListener.
 * Because WorkOrderAssignmentService publishes the event from inside its
 * @Transactional method, Spring's default (synchronous) multicaster invokes
 * this method inline, on the publisher's thread, inside the publisher's
 * transaction and Hibernate session.
 *
 * Three consequences make this the right tool for atomic cross-domain work:
 *
 *   1. Reads see in-flight data. WorkOrder.get(...) returns the managed
 *      instance from the current session - including the assignee that was
 *      added moments ago in this same transaction.
 *   2. Writes join the same commit. Mutating WorkOrder and CustomerRequest
 *      here flushes as part of the publisher's commit.
 *   3. Failure rolls everything back. Any exception thrown here propagates
 *      out through publishEvent(...) and unwinds the entire operation - the
 *      assignee insert included.
 *
 * Note what is deliberately absent: there is NO @Transactional on this class.
 * The listener does not open or manage a transaction of its own - it runs
 * inside the one the publisher already started, which is the whole point.
 * (Adding Grails' @Transactional here would also be actively harmful: it is a
 * compile-time AST transform that relocates the method body, which hides the
 * @EventListener annotation from Spring's listener scanner so the listener
 * never registers.)
 */
class WorkOrderAssignmentListener {

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

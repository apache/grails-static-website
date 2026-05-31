package example

import example.events.EmployeeAssignedEvent
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher

/**
 * Assigns an employee to a work order. The persistence work and the event
 * publish both happen inside one @Transactional method.
 *
 * Contrast with OrderService: there the listeners run AFTER this method
 * commits, in their own transactions. Here the event is consumed by a plain
 * @EventListener, which Spring dispatches SYNCHRONOUSLY at the publishEvent
 * call - on this thread, inside this transaction, before this method returns.
 * So the assignee insert, plus every change the listener makes, are one
 * atomic unit: they all commit together or all roll back together.
 */
@Transactional
class WorkOrderAssignmentService {

    @Autowired
    ApplicationEventPublisher applicationEventPublisher

    WorkOrderAssignee assignEmployee(Long workOrderId, String employeeName) {
        WorkOrder workOrder = WorkOrder.get(workOrderId)
        if (!workOrder) {
            throw new IllegalArgumentException("Unknown work order: ${workOrderId}")
        }

        WorkOrderAssignee assignee = new WorkOrderAssignee(
                workOrder: workOrder, employeeName: employeeName)
                .save(failOnError: true)

        // Synchronous dispatch: the @EventListener for this event runs inline,
        // right here, inside this transaction and Hibernate session. If it
        // throws, the exception propagates back through this call and rolls
        // the whole method - including the assignee insert above - back.
        applicationEventPublisher.publishEvent(
                new EmployeeAssignedEvent(workOrder.id, assignee.id, employeeName))

        assignee
    }
}

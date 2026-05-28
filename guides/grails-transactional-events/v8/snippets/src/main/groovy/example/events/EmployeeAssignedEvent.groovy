package example.events

import groovy.transform.Immutable

/**
 * Published by WorkOrderAssignmentService the moment a WorkOrderAssignee is
 * created. Unlike OrderPlacedEvent (consumed AFTER_COMMIT), this event is
 * consumed SYNCHRONOUSLY, inside the publisher's transaction, by a plain
 * @EventListener. It still carries IDs rather than entity references, but
 * the listener that handles it runs in the same Hibernate session, so a
 * WorkOrder.get(workOrderId) returns the live, in-flight instance.
 */
@Immutable
class EmployeeAssignedEvent {
    Long workOrderId
    Long assigneeId
    String employeeName
}

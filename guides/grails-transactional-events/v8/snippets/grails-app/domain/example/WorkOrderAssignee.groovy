package example

import grails.persistence.Entity

/**
 * Joins an employee to a WorkOrder. Creating one of these is the business
 * event that kicks off the whole orchestration: the assignment, the work
 * order's status change, and the customer request's status change all
 * commit together or not at all.
 */
@Entity
class WorkOrderAssignee {

    WorkOrder workOrder
    String employeeName
    Date dateCreated

    static belongsTo = [workOrder: WorkOrder]

    static constraints = {
        employeeName blank: false, maxSize: 255
    }
}

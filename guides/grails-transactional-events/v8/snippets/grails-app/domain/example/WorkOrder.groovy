package example

import grails.persistence.Entity

/**
 * A unit of work belonging to a CustomerRequest. Starts OPEN; moves to
 * PLANNED the moment an employee is assigned. The status change is driven
 * by the synchronous orchestration listener, inside the same transaction
 * that created the assignment.
 */
@Entity
class WorkOrder {

    CustomerRequest customerRequest
    String description
    WorkOrderStatus status = WorkOrderStatus.OPEN

    static belongsTo = [customerRequest: CustomerRequest]

    static constraints = {
        description blank: false, maxSize: 255
        status      nullable: false
    }

    static mapping = {
        // 'order' is reserved in most SQL dialects; the prefixed table name
        // sidesteps the same family of keyword collisions for work orders.
        table 'work_orders'
    }
}

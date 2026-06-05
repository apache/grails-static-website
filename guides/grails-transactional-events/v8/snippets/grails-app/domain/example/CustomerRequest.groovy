package example

import grails.persistence.Entity

/**
 * A request raised by a customer. One CustomerRequest spawns one or more
 * WorkOrders. Its status is advanced by the orchestration listener as the
 * work underneath it progresses - never edited directly from a controller.
 */
@Entity
class CustomerRequest {

    String summary
    CustomerRequestStatus status = CustomerRequestStatus.SUBMITTED

    static constraints = {
        summary blank: false, maxSize: 255
        status  nullable: false
    }

    String toString() { summary }
}

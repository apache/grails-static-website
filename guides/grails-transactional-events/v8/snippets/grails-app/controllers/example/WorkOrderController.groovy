package example

import groovy.json.JsonOutput
import org.springframework.http.HttpStatus

/**
 * REST entry point for the synchronous orchestration. assign() delegates to
 * WorkOrderAssignmentService, whose @Transactional method publishes
 * EmployeeAssignedEvent; the plain @EventListener cascades the WorkOrder and
 * CustomerRequest status changes inline, in the same transaction. A business
 * rule violation (assigning work to a closed request) surfaces as 409 and
 * rolls the whole operation back.
 */
class WorkOrderController {

    WorkOrderAssignmentService workOrderAssignmentService

    def show() {
        Long workOrderId = params.long('workOrderId')
        WorkOrder workOrder = WorkOrder.get(workOrderId)
        if (!workOrder) {
            response.status = HttpStatus.NOT_FOUND.value()
            render(text: JsonOutput.toJson([error: "No work order ${workOrderId}".toString()]),
                    contentType: 'application/json')
            return
        }
        render(text: JsonOutput.toJson([
                id                   : workOrder.id,
                status               : workOrder.status.name(),
                customerRequestId    : workOrder.customerRequest.id,
                customerRequestStatus: workOrder.customerRequest.status.name()
        ]), contentType: 'application/json')
    }

    def assign() {
        Long workOrderId = params.long('workOrderId')
        try {
            WorkOrderAssignee assignee = workOrderAssignmentService.assignEmployee(workOrderId, params.employeeName)
            WorkOrder workOrder = WorkOrder.get(workOrderId)
            response.status = HttpStatus.CREATED.value()
            render(text: JsonOutput.toJson([
                    assigneeId           : assignee.id,
                    workOrderId          : workOrder.id,
                    workOrderStatus      : workOrder.status.name(),
                    customerRequestId    : workOrder.customerRequest.id,
                    customerRequestStatus: workOrder.customerRequest.status.name()
            ]), contentType: 'application/json')
        } catch (IllegalStateException e) {
            // The orchestration rolled the whole transaction back before this
            // catch ran; we just translate it into a clean 409 response.
            response.status = HttpStatus.CONFLICT.value()
            render(text: JsonOutput.toJson([error: e.message]), contentType: 'application/json')
        }
    }
}

package example

/**
 * Minimal HTML UI for the work-order assignment flow, used by the Geb
 * browser tests. Renders plain HTML inline (no GSP) so the rest-api profile
 * stays untouched. index() shows the current status plus an assignment form;
 * assign() runs the synchronous orchestration and re-renders the page with
 * the updated (or, on a closed request, unchanged) status and a message.
 */
class WorkOrderUiController {

    WorkOrderAssignmentService workOrderAssignmentService

    def index() {
        render(text: page(params.long('workOrderId'), null), contentType: 'text/html')
    }

    def assign() {
        Long workOrderId = params.long('workOrderId')
        String message
        try {
            workOrderAssignmentService.assignEmployee(workOrderId, params.employeeName)
            message = "Assigned ${params.employeeName}"
        } catch (IllegalStateException e) {
            message = e.message
        }
        render(text: page(workOrderId, message), contentType: 'text/html')
    }

    private String page(Long workOrderId, String message) {
        WorkOrder workOrder = WorkOrder.get(workOrderId)
        if (!workOrder) {
            return '<html><body><h1>Work Order not found</h1></body></html>'
        }
        String messageHtml = message ? "<p id=\"message\">${message.encodeAsHTML()}</p>" : ''
        """<!doctype html>
<html>
<head><title>Work Order ${workOrder.id}</title></head>
<body>
  <h1>Work Order ${workOrder.id}</h1>
  <p>Work order status: <span id="workOrderStatus">${workOrder.status.name()}</span></p>
  <p>Request status: <span id="customerRequestStatus">${workOrder.customerRequest.status.name()}</span></p>
  ${messageHtml}
  <form method="post" action="/ui">
    <input type="hidden" name="workOrderId" value="${workOrder.id}"/>
    <label for="employeeName">Employee</label>
    <input type="text" id="employeeName" name="employeeName"/>
    <button type="submit" id="assignBtn">Assign</button>
  </form>
</body>
</html>"""
    }
}

package example.pages

import geb.Page

/**
 * Geb page object for the work-order assignment screen served by
 * WorkOrderUiController. Navigated to with a workOrderId query param:
 * to(WorkOrderPage, workOrderId: 123).
 */
class WorkOrderPage extends Page {

    static url = '/ui'

    static at = { $('h1').text().startsWith('Work Order') }

    static content = {
        workOrderStatus { $('#workOrderStatus').text() }
        requestStatus { $('#customerRequestStatus').text() }
        employeeNameField { $('#employeeName') }
        assignButton { $('#assignBtn') }
        message(required: false) { $('#message') }
    }

    void assignEmployee(String name) {
        employeeNameField.value(name)
        assignButton.click()
    }
}

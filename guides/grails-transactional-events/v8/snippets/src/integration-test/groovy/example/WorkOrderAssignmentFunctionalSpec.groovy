package example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * End-to-end functional test of the synchronous orchestration over real HTTP.
 * The happy path proves the cascade commits as one unit; the 409 path proves
 * a business-rule violation rolls the whole operation back - assignee, work
 * order status, and request status all unchanged - through the full web stack.
 */
@Integration
class WorkOrderAssignmentFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    private final HttpClient client = HttpClient.newHttpClient()

    void "POST /assignments plans the work order and advances the request"() {
        given:
        Long[] ids = createScenario(CustomerRequestStatus.SUBMITTED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]

        when:
        HttpResponse<String> resp = post('/assignments', "workOrderId=${workOrderId}&employeeName=Dana+Reed")

        then: 'the POST reports the atomic cascade'
        resp.statusCode() == 201
        with(new JsonSlurper().parseText(resp.body())) {
            workOrderStatus == 'PLANNED'
            customerRequestStatus == 'IN_PROGRESS'
        }

        and: 'a fresh GET confirms the committed state'
        with(new JsonSlurper().parseText(get("/workOrderStatus?workOrderId=${workOrderId}").body())) {
            status == 'PLANNED'
            customerRequestStatus == 'IN_PROGRESS'
        }

        cleanup:
        deleteScenario(workOrderId, requestId)
    }

    void "POST against a closed request returns 409 and rolls every change back"() {
        given:
        Long[] ids = createScenario(CustomerRequestStatus.CANCELLED)
        Long requestId = ids[0]
        Long workOrderId = ids[1]
        long assigneesBefore = countAssignees(workOrderId)

        when:
        HttpResponse<String> resp = post('/assignments', "workOrderId=${workOrderId}&employeeName=Dana+Reed")

        then: 'the endpoint reports the conflict'
        resp.statusCode() == 409

        and: 'nothing persisted - no assignee, both statuses untouched'
        countAssignees(workOrderId) == assigneesBefore
        WorkOrder.withNewTransaction { WorkOrder.get(workOrderId).status } == WorkOrderStatus.OPEN
        CustomerRequest.withNewTransaction { CustomerRequest.get(requestId).status } == CustomerRequestStatus.CANCELLED

        cleanup:
        deleteScenario(workOrderId, requestId)
    }

    private Long[] createScenario(CustomerRequestStatus requestStatus) {
        Long requestId = null
        Long workOrderId = null
        CustomerRequest.withNewTransaction {
            CustomerRequest request = new CustomerRequest(
                    summary: 'Replace the rooftop HVAC unit', status: requestStatus)
                    .save(flush: true, failOnError: true)
            WorkOrder workOrder = new WorkOrder(
                    customerRequest: request, description: 'Site survey and install')
                    .save(flush: true, failOnError: true)
            requestId = request.id
            workOrderId = workOrder.id
        }
        [requestId, workOrderId] as Long[]
    }

    private long countAssignees(Long workOrderId) {
        WorkOrderAssignee.withNewTransaction {
            WorkOrderAssignee.where { workOrder.id == workOrderId }.count()
        }
    }

    private void deleteScenario(Long workOrderId, Long requestId) {
        WorkOrder.withNewTransaction {
            WorkOrder workOrder = WorkOrder.get(workOrderId)
            if (workOrder) {
                WorkOrderAssignee.findAllByWorkOrder(workOrder)*.delete()
                workOrder.delete(flush: true)
            }
            CustomerRequest.get(requestId)?.delete(flush: true)
        }
    }

    private HttpResponse<String> post(String path, String form) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}${path}"))
                .header('Content-Type', 'application/x-www-form-urlencoded')
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build()
        client.send(req, HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> get(String path) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}${path}"))
                .GET()
                .build()
        client.send(req, HttpResponse.BodyHandlers.ofString())
    }
}

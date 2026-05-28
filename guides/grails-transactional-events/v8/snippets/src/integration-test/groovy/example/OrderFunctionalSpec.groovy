package example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * End-to-end functional test: a real HTTP POST to /orders against the running
 * application, proving the AFTER_COMMIT fan-out fires through the full
 * web -> service -> listener -> DB stack (not just a service call in a test).
 */
@Integration
class OrderFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    private final HttpClient client = HttpClient.newHttpClient()

    void "POST /orders places the order and runs both AFTER_COMMIT listeners"() {
        given:
        Customer customer = null
        Customer.withNewTransaction {
            customer = new Customer(name: 'Web Buyer',
                    email: "web-${System.nanoTime()}@example.com").save(flush: true)
        }
        long auditBefore = AuditLog.withNewTransaction { AuditLog.count() }

        when:
        HttpResponse<String> resp = post('/orders', "customerId=${customer.id}&total=42.00")

        then: 'the endpoint returns 201 with the created order'
        resp.statusCode() == 201
        with(new JsonSlurper().parseText(resp.body())) {
            customerId == customer.id
            total == 42.00
        }

        and: 'both AFTER_COMMIT listeners fired end-to-end'
        AuditLog.withNewTransaction { AuditLog.count() } == auditBefore + 1
        Customer.withNewTransaction { Customer.get(customer.id).lifetimeValue } == new BigDecimal('42.00')

        cleanup:
        Order.withNewTransaction {
            AuditLog.where { customerId == customer.id }.deleteAll()
            Order.findAllByCustomer(Customer.load(customer.id))*.delete()
            Customer.get(customer.id)?.delete(flush: true)
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
}

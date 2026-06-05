package example

import groovy.json.JsonOutput
import org.springframework.http.HttpStatus

/**
 * REST entry point for placing an order. Delegates to OrderService, whose
 * @Transactional method publishes OrderPlacedEvent; the AFTER_COMMIT
 * listeners then fan out once this request's transaction commits.
 */
class OrderController {

    OrderService orderService

    def place() {
        Order order = orderService.placeOrder(params.long('customerId'), params.total as BigDecimal)
        response.status = HttpStatus.CREATED.value()
        render(text: JsonOutput.toJson([
                id        : order.id,
                customerId: order.customer.id,
                total     : order.total
        ]), contentType: 'application/json')
    }
}

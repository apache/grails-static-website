package example.grails

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher

@GrailsCompileStatic
@Transactional
class DeliveryService {
    @Autowired
    ApplicationEventPublisher applicationEventPublisher

    Delivery create(String reference) {
        Delivery delivery = new Delivery(reference: reference)
        if (!delivery.validate()) {
            return delivery
        }
        delivery.save(failOnError: true, flush: true)
        applicationEventPublisher.publishEvent(new DeliveryCreatedEvent(delivery.id))
        delivery
    }
}

package example.grails

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class DeliveryController {
    static allowedMethods = [create: 'POST', show: 'GET']
    static responseFormats = ['json']

    DeliveryService deliveryService

    Object create(String reference) {
        Delivery delivery = deliveryService.create(reference)
        if (delivery.hasErrors()) {
            respond(delivery.errors, status: 422)
            return
        }
        respond(delivery, status: 201)
    }

    Object show(Long id) {
        Delivery delivery = Delivery.get(id)
        if (delivery == null) {
            render status: 404
            return
        }
        respond(delivery)
    }
}

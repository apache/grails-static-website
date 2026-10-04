package example.grails

import grails.testing.mixin.integration.Integration
import grails.core.GrailsApplication
import grails.core.GrailsControllerClass
import grails.web.mapping.UrlMappingInfo
import grails.web.mapping.UrlMappingsHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import spock.lang.Specification

@Integration
class DeliveryHttpApiSpec extends Specification {
    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    UrlMappingsHolder urlMappingsHolder

    @Value('${local.server.port}')
    int port

    void 'POST deliveries routes to the documented create action'() {
        given:
        GrailsControllerClass deliveryController = grailsApplication.getArtefact('Controller', DeliveryController.name)
        UrlMappingInfo[] deliveryMappings = urlMappingsHolder.matchAll('/deliveries', HttpMethod.POST)

        when:
        HttpURLConnection connection = new URL("http://localhost:${port}/deliveries").openConnection()
        connection.requestMethod = 'POST'
        connection.doOutput = true
        connection.setRequestProperty('Accept', 'application/json')
        connection.setRequestProperty('Content-Type', 'application/x-www-form-urlencoded')
        connection.outputStream.withCloseable { output ->
            output.write('reference=delivery-http-route'.bytes)
        }

        then:
        deliveryController.actions.contains('create')
        deliveryMappings.any { it.controllerName == 'delivery' && it.actionName == 'create' }
        connection.responseCode == 201

        cleanup:
        connection.disconnect()
    }

    void 'POST deliveries without a reference returns 422 and does not persist a delivery'() {
        given:
        int deliveryCount = Delivery.withNewSession { Delivery.count() }

        when:
        HttpURLConnection connection = new URL("http://localhost:${port}/deliveries").openConnection()
        connection.requestMethod = 'POST'
        connection.doOutput = true
        connection.setRequestProperty('Accept', 'application/json')
        connection.setRequestProperty('Content-Type', 'application/x-www-form-urlencoded')
        connection.outputStream.withCloseable { output ->
            output.write(''.bytes)
        }

        then:
        connection.responseCode == 422
        Delivery.withNewSession { Delivery.count() } == deliveryCount

        cleanup:
        connection?.disconnect()
    }
}

package example.grails

import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import spock.lang.Specification

class JobExamplesControllerSpec extends Specification implements ControllerUnitTest<JobExamplesController> {
    void 'immediate job endpoint returns an accepted response with a string job id'() {
        given:
        controller.jobExamplesService = Stub(JobExamplesService) {
            enqueueImmediately(7L) >> 'b5ab0831-a3af-4554-9b7a-5571137e9aaa'
        }
        request.method = 'POST'
        request.addHeader('Accept', 'application/json')

        when:
        controller.immediate(7L)

        then:
        response.status == 202
        new JsonSlurper().parseText(response.text).jobId == 'b5ab0831-a3af-4554-9b7a-5571137e9aaa'
    }

    void 'recurring job endpoint requires a delivery id and returns its stable identifier'() {
        given:
        controller.jobExamplesService = Stub(JobExamplesService) {
            registerRecurringDelivery(19L) >> 'delivery-19'
        }
        request.method = 'POST'
        request.addHeader('Accept', 'application/json')

        when:
        controller.recurring(19L)

        then:
        response.status == 202
        new JsonSlurper().parseText(response.text).recurringJobId == 'delivery-19'
    }
}

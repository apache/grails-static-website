package example.grails.jobrunr

import spock.lang.Specification

class JobRequestSpec extends Specification {
    void 'process request maps to its Spring handler and supports deserialization'() {
        when:
        ProcessDeliveryJobRequest request = new ProcessDeliveryJobRequest(42L)
        ProcessDeliveryJobRequest emptyRequest = new ProcessDeliveryJobRequest()

        then:
        request.deliveryId == 42L
        emptyRequest.deliveryId == null
        request.jobRequestHandler == DeliveryJobRequestHandler
    }

    void 'retry request maps to its dedicated handler'() {
        expect:
        new RetryDeliveryJobRequest(7L).jobRequestHandler == RetryDeliveryJobRequestHandler
    }
}

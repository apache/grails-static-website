package example.grails.jobrunr

import groovy.transform.CompileStatic
import org.jobrunr.jobs.lambdas.JobRequest

@CompileStatic
class ProcessDeliveryJobRequest implements JobRequest {
    Long deliveryId

    ProcessDeliveryJobRequest() {
    }

    ProcessDeliveryJobRequest(Long deliveryId) {
        this.deliveryId = deliveryId
    }

    @Override
    Class<DeliveryJobRequestHandler> getJobRequestHandler() {
        DeliveryJobRequestHandler
    }
}

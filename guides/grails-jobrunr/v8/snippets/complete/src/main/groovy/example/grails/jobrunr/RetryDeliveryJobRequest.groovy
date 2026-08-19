package example.grails.jobrunr

import groovy.transform.CompileStatic
import org.jobrunr.jobs.lambdas.JobRequest

@CompileStatic
class RetryDeliveryJobRequest implements JobRequest {
    Long deliveryId

    RetryDeliveryJobRequest() {
    }

    RetryDeliveryJobRequest(Long deliveryId) {
        this.deliveryId = deliveryId
    }

    @Override
    Class<RetryDeliveryJobRequestHandler> getJobRequestHandler() {
        RetryDeliveryJobRequestHandler
    }
}

package example.grails.jobrunr

import grails.compiler.GrailsCompileStatic
import org.jobrunr.jobs.annotations.Job
import org.jobrunr.jobs.lambdas.JobRequestHandler
import org.springframework.stereotype.Component

@Component
@GrailsCompileStatic
class RetryDeliveryJobRequestHandler implements JobRequestHandler<RetryDeliveryJobRequest> {
    @Override
    @Job(name = 'Retry delivery example', retries = 2, labels = ['delivery', 'retry'])
    void run(RetryDeliveryJobRequest request) {
        throw new IllegalStateException("Retry example for delivery ${request.deliveryId}")
    }
}

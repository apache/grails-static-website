package example.grails

import example.grails.jobrunr.ProcessDeliveryJobRequest
import example.grails.jobrunr.RetryDeliveryJobRequest
import grails.compiler.GrailsCompileStatic
import java.time.Instant
import org.jobrunr.scheduling.JobRequestScheduler
import org.springframework.beans.factory.annotation.Autowired

@GrailsCompileStatic
class JobExamplesService {
    @Autowired
    JobRequestScheduler jobRequestScheduler

    String enqueueImmediately(Long deliveryId) {
        jobRequestScheduler.enqueue(new ProcessDeliveryJobRequest(deliveryId)).asUUID().toString()
    }

    String enqueueForLater(Long deliveryId) {
        jobRequestScheduler.schedule(Instant.now().plusSeconds(30), new ProcessDeliveryJobRequest(deliveryId)).asUUID().toString()
    }

    String registerRecurringDelivery(Long deliveryId) {
        jobRequestScheduler.scheduleRecurrently(
            "delivery-${deliveryId}",
            '0 3 * * *',
            new ProcessDeliveryJobRequest(deliveryId)
        )
    }

    String enqueueRetryDemo(Long deliveryId) {
        jobRequestScheduler.enqueue(new RetryDeliveryJobRequest(deliveryId)).asUUID().toString()
    }
}

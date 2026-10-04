package example.grails

import example.grails.jobrunr.ProcessDeliveryJobRequest
import grails.compiler.GrailsCompileStatic
import org.jobrunr.scheduling.JobRequestScheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
@GrailsCompileStatic
class DeliveryJobEnqueuer {
    @Autowired
    JobRequestScheduler jobRequestScheduler

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void enqueue(DeliveryCreatedEvent event) {
        jobRequestScheduler.enqueue(new ProcessDeliveryJobRequest(event.deliveryId))
    }
}

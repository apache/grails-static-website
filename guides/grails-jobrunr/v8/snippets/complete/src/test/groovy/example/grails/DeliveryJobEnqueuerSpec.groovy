package example.grails

import org.jobrunr.scheduling.JobRequestScheduler
import spock.lang.Specification
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class DeliveryJobEnqueuerSpec extends Specification {
    void 'listener runs only after the delivery transaction commits'() {
        when:
        TransactionalEventListener annotation = DeliveryJobEnqueuer
            .getMethod('enqueue', DeliveryCreatedEvent)
            .getAnnotation(TransactionalEventListener)

        then:
        annotation != null
        annotation.phase() == TransactionPhase.AFTER_COMMIT
        !annotation.fallbackExecution()
    }

    void 'listener maps a committed delivery event to a JobRequest'() {
        given:
        JobRequestScheduler scheduler = Mock()
        DeliveryJobEnqueuer enqueuer = new DeliveryJobEnqueuer(jobRequestScheduler: scheduler)

        when:
        enqueuer.enqueue(new DeliveryCreatedEvent(17L))

        then:
        1 * scheduler.enqueue({ request -> request.deliveryId == 17L })
    }
}

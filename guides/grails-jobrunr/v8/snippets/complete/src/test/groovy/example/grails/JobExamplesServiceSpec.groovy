package example.grails

import example.grails.jobrunr.ProcessDeliveryJobRequest
import org.jobrunr.scheduling.JobRequestScheduler
import spock.lang.Specification

class JobExamplesServiceSpec extends Specification {
    void 'recurring delivery uses the delivery id for its stable JobRunr identifier and request'() {
        given:
        JobRequestScheduler scheduler = Mock()
        JobExamplesService service = new JobExamplesService(jobRequestScheduler: scheduler)

        when:
        String recurringJobId = service.registerRecurringDelivery(19L)

        then:
        recurringJobId == 'delivery-19'
        1 * scheduler.scheduleRecurrently(
            'delivery-19',
            '0 3 * * *',
            { ProcessDeliveryJobRequest request -> request.deliveryId == 19L }
        ) >> 'delivery-19'
    }
}

package example.grails.jobrunr

import example.grails.Delivery
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import org.jobrunr.jobs.annotations.Job
import org.jobrunr.jobs.lambdas.JobRequestHandler
import org.jobrunr.server.runner.ThreadLocalJobContext
import org.springframework.stereotype.Component

@Component
@GrailsCompileStatic
@Transactional
class DeliveryJobRequestHandler implements JobRequestHandler<ProcessDeliveryJobRequest> {
    @Override
    @Job(name = 'Process delivery', retries = 3, labels = ['delivery'])
    void run(ProcessDeliveryJobRequest request) throws Exception {
        try {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException('Delivery worker was interrupted')
            }

            if (request.deliveryId == null) {
                return
            }

            Delivery delivery = Delivery.get(request.deliveryId)
            if (delivery == null || delivery.status == 'SUCCEEDED') {
                return
            }

            def context = ThreadLocalJobContext.getJobContext()
            def progressBar = context.progressBar(3)
            for (int step = 1; step <= 3; step++) {
                delivery.progress = step * 33
                progressBar.incrementSucceeded()
            }
            delivery.progress = 100
            delivery.status = 'SUCCEEDED'
            delivery.completedAt = new Date()
            delivery.save(failOnError: true, flush: true)
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt()
            throw exception
        }
    }
}

package example.grails

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class JobExamplesController {
    static allowedMethods = [immediate: 'POST', delayed: 'POST', recurring: 'POST', retry: 'POST']
    static responseFormats = ['json']

    JobExamplesService jobExamplesService

    Object immediate(Long id) {
        respond([jobId: jobExamplesService.enqueueImmediately(id)], status: 202)
    }

    Object delayed(Long id) {
        respond([jobId: jobExamplesService.enqueueForLater(id)], status: 202)
    }

    Object recurring(Long id) {
        respond([recurringJobId: jobExamplesService.registerRecurringDelivery(id)], status: 202)
    }

    Object retry(Long id) {
        respond([jobId: jobExamplesService.enqueueRetryDemo(id)], status: 202)
    }
}

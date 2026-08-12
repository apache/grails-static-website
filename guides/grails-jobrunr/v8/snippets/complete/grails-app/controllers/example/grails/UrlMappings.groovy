package example.grails

class UrlMappings {
    static mappings = {
        "/deliveries"(controller: 'delivery', action: 'create', method: 'POST')
        "/deliveries/$id"(controller: 'delivery', action: 'show', method: 'GET')
        "/jobs/immediate/$id"(controller: 'jobExamples', action: 'immediate', method: 'POST')
        "/jobs/delayed/$id"(controller: 'jobExamples', action: 'delayed', method: 'POST')
        "/jobs/recurring/$id"(controller: 'jobExamples', action: 'recurring', method: 'POST')
        "/jobs/retry/$id"(controller: 'jobExamples', action: 'retry', method: 'POST')
    }
}

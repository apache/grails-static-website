package example.grails

import groovy.transform.CompileStatic

@CompileStatic
class AnalyticsController {
    BookAnalyticsGormService bookAnalyticsGormService

    def index() {
        [analytics: bookAnalyticsGormService.findAll()]
    }
}
package example.grails

import groovy.transform.CompileStatic
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
@Requires(notEnv = Environment.TEST) // <1>
@KafkaListener // <2>
class AnalyticsListener {

    private final BookAnalyticsGormService bookAnalyticsGormService // <3>

    AnalyticsListener(BookAnalyticsGormService bookAnalyticsGormService) { // <3>
        this.bookAnalyticsGormService = bookAnalyticsGormService
    }

    @Topic('analytics') // <4>
    void updateAnalytics(Map payload) {

        if (payload.containsKey('isbn')) {
            BookAnalytics bookAnalytics = bookAnalyticsGormService.findByIsbn(payload.isbn as String)
            if (bookAnalytics) {
                bookAnalyticsGormService.updateCount(payload.isbn as String, bookAnalytics.count + 1)
            } else {
                bookAnalytics = new BookAnalytics(isbn: payload.isbn as String, count: 1L)
                bookAnalyticsGormService.saveBookAnalytics(bookAnalytics)
            }
        }
    }
}

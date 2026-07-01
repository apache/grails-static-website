package example.grails

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.Topic

@KafkaClient
interface AnalyticsClient {

    @Topic('analytics') // <1>
    Map updateAnalytics(Map book) // <2>
}

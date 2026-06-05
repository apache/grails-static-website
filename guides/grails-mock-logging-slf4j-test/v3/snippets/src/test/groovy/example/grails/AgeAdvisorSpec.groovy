package example.grails

import com.google.common.collect.ImmutableList
import example.grails.AgeAdvisor
import spock.lang.Shared
import spock.lang.Specification
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

class AgeAdvisorSpec extends Specification {

    @Shared
    AgeAdvisor ageAdvisor = new AgeAdvisor()
    @Shared
    TestLogger logger = TestLoggerFactory.getTestLogger("example.grails.AgeAdvisor") // <1>

    def cleanup() {
        TestLoggerFactory.clear()
    }

    void "verify young age logs"() {

        when: "method is invoked"
        ageAdvisor.offerAgeAdvice(15)
        ImmutableList<LoggingEvent> loggingEvents = logger.getLoggingEvents()

        then: "check the logging events"
        loggingEvents.size() == 2 // <2>
        loggingEvents[0].message == "You are a young and vibrant!"
        loggingEvents[0].level == Level.INFO
        loggingEvents[1].message == "Live life to the fullest."
        loggingEvents[1].level == Level.INFO
    }

    void "verify old age logs"() {

        when: "method is invoked"
        ageAdvisor.offerAgeAdvice(31)
        ImmutableList<LoggingEvent> loggingEvents = logger.getLoggingEvents()

        then: "check the logging events"
        loggingEvents.size() == 1
        loggingEvents[0].message == "It's all downhill from here, sorry."
        loggingEvents[0].level == Level.WARN
    }
}

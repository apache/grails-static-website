// tag::PersonControllerIntSpecBegin[]
package example.grails

import com.google.common.collect.ImmutableList
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Shared
import spock.lang.Specification
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

@Integration
@Rollback
class PersonControllerIntSpec extends Specification {

    @Shared
    TestLogger personControllerLogger = TestLoggerFactory.getTestLogger("example.grails.PersonController") // <1>
    @Shared
    HttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    def cleanup() {
        TestLoggerFactory.clearAll() // <2>
    }
// end::PersonControllerIntSpecBegin[]

    // tag::PersonControllerIntSpecCreateSuccess[]
    void "test create person successful logs"() {
        when:"a person is created"
        HttpResponse<Map> resp = client.toBlocking().exchange(HttpRequest.GET("/person/createPerson?name=Nirav&age=40"), Map)
        ImmutableList<LoggingEvent> loggingEvents = personControllerLogger.getAllLoggingEvents() // <3>

        then: "check the logging events"
        resp.status == HttpStatus.OK
        loggingEvents.size() == 1 // <4>
        loggingEvents[0].message == "person saved successfully: name: Nirav, age: 40" // <5>
        loggingEvents[0].level == Level.INFO
    }
    // end::PersonControllerIntSpecCreateSuccess[]

    // tag::PersonControllerIntSpecCreateUnsuccess[]
    void "test create person unsuccessful logs"() {
        when:"a person is created, but has a input error"
        HttpResponse<Map> resp = client.toBlocking().exchange(HttpRequest.GET("/person/createPerson?name=Bob&age=Twenty"), Map)
        ImmutableList<LoggingEvent> loggingEvents = personControllerLogger.getAllLoggingEvents()

        then: "check the logging events"
        resp.status == HttpStatus.OK
        loggingEvents.size() == 1
        loggingEvents[0].message == "Error occurred on save!"
        loggingEvents[0].level == Level.ERROR
    }
    // end::PersonControllerIntSpecCreateUnsuccess[]

    // tag::PersonControllerIntSpecAdvice[]
    void "test offerAdvice to old person"() {
        given: "A person is already created"
        HttpResponse<Map> resp = client.toBlocking().exchange(HttpRequest.GET("/person/createPerson?name=John&age=35"), Map)
        TestLogger ageAdvisorLogger = TestLoggerFactory.getTestLogger("example.grails.AgeAdvisor") // <1>

        when:"we ask for advice"
        resp = client.toBlocking().exchange(HttpRequest.GET("/person/offerAdvice?name=John"), Map)
        ImmutableList<LoggingEvent> loggingEvents = ageAdvisorLogger.getAllLoggingEvents()

        then: "check the logging events"
        resp.status == HttpStatus.OK
        loggingEvents.size() == 1
        loggingEvents[0].message == "It's all downhill from here, sorry."
        loggingEvents[0].level == Level.WARN
    }
    // end::PersonControllerIntSpecAdvice[]

// tag::PersonControllerIntSpecEnd[]
}
// end::PersonControllerIntSpecEnd[]

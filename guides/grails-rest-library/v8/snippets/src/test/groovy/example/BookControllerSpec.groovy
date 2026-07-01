package example

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Controller unit test for the pagination clamping the custom index action
 * applies before it queries. No Spring context, no real database.
 */
class BookControllerSpec extends Specification
        implements ControllerUnitTest<BookController>, DataTest {

    Class[] getDomainClassesToMock() { [Author, Book] }

    @Unroll
    void "index clamps max #requested to #expected"() {
        when:
        controller.index(requested)

        then:
        controller.params.max == expected

        where:
        requested || expected
        500       || 100   // capped at 100
        10        || 10    // within range, untouched
        null      || 25    // default
        -5        || 25    // negative treated as default
    }

    void "index never produces a negative offset"() {
        given:
        controller.params.offset = -10

        when:
        controller.index(25)

        then:
        controller.params.offset == 0
    }
}

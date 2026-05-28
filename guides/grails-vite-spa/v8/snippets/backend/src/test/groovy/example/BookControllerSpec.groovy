package example

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

/**
 * Controller unit test for the page-size clamp the overridden
 * listAllResources applies. It reads params.int('max', 25), so the spec
 * drives it through the controller's GrailsParameterMap (a plain Map has
 * no int() accessor). No Spring context boots.
 */
class BookControllerSpec extends Specification
        implements ControllerUnitTest<BookController>, DataTest {

    Class[] getDomainClassesToMock() { [Book] }

    void "listAllResources clamps max to 100"() {
        given:
        controller.params.max = '500'

        when:
        controller.listAllResources(controller.params)

        then:
        controller.params.max == 100
    }

    void "listAllResources keeps a max within range"() {
        given:
        controller.params.max = '10'

        when:
        controller.listAllResources(controller.params)

        then:
        controller.params.max == 10
    }

    void "listAllResources defaults max to 25 when absent"() {
        when:
        controller.listAllResources(controller.params)

        then:
        controller.params.max == 25
    }
}

package demo

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED
import static javax.servlet.http.HttpServletResponse.SC_OK
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitPublicProperty'])
class StudentControllerAllowedMethodsSpec extends Specification implements ControllerUnitTest<StudentController> {

    @Unroll
    def "StudentController.save does not accept #method requests"(String method) {
        when:
        request.method = method
        controller.save()

        then:
        response.status == SC_METHOD_NOT_ALLOWED

        where:
        method << ['PATCH', 'DELETE', 'GET', 'PUT']
    }

    def "StudentController.save accepts POST requests"() {
        when:
        request.method = 'POST'
        controller.save()

        then:
        response.status == SC_OK
    }
}

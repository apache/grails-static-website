package demo

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.lang.Unroll
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

class BookControllerSpec extends Specification implements ControllerUnitTest<BookController> {

    @Unroll
    def "test BookController.search does not accept #method requests"(String method) {
        when:
        request.method = method
        controller.search()

        then:
        response.status == SC_METHOD_NOT_ALLOWED

        where:
        method << ['PATCH', 'DELETE', 'POST', 'PUT']
    }

    def "test BookController.search accepts GET requests"() {
        when:
        request.method = 'GET'
        controller.search()

        then:
        response.status == SC_NOT_FOUND
    }
}
package example

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification

/**
 * ControllerUnitTest<BookController> wires up just enough of the web
 * layer to exercise action routing, model preparation, and response
 * status codes. Adding DataTest (with the right domain mocked) lets
 * the RestfulController's auto-generated CRUD actions actually find
 * persisted entities through GORM.
 */
class BookControllerSpec extends Specification
        implements ControllerUnitTest<BookController>, DataTest {

    Class[] getDomainClassesToMock() { [Book] }

    void "GET /books/{id} returns the book as JSON"() {
        given:
        Book b = new Book(title: 'The Hobbit', isbn: '9780547928227', pageCount: 310)
                  .save(failOnError: true, flush: true)

        when: 'RestfulController.show() reads params.id - it takes no argument'
        request.method = 'GET'
        params.id = b.id
        controller.show()

        then:
        response.status == HttpStatus.OK.value()
        response.json.title == 'The Hobbit'
    }

    void "GET /books/{id} for a missing id returns 404"() {
        when:
        request.method = 'GET'
        params.id = 99999L
        controller.show()

        then:
        response.status == HttpStatus.NOT_FOUND.value()
    }

    void "POST /books with a malformed ISBN returns 422"() {
        given:
        request.method = 'POST'
        request.json = [title: 'X', isbn: 'not-an-isbn']

        when:
        controller.save()

        then:
        response.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
    }
}

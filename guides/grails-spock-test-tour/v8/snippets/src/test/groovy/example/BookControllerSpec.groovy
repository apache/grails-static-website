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

    void "GET /books returns the book list as JSON"() {
        given:
        new Book(title: 'First', isbn: '9780547928227', pageCount: 100)
            .save(flush: true, failOnError: true)
        new Book(title: 'Second', isbn: '9780547928228', pageCount: 200)
            .save(flush: true, failOnError: true)

        when:
        request.method = 'GET'
        request.format = 'json'
        controller.index(10)

        then:
        response.status == HttpStatus.OK.value()
        response.contentAsString != null
    }

    void "POST /books with valid data saves and returns 201"() {
        given:
        request.method = 'POST'
        request.json = [title: 'Valid Book', isbn: '9780547928228', pageCount: 150]

        when:
        controller.save()

        then:
        response.status == HttpStatus.CREATED.value()
        response.json.title == 'Valid Book'
        response.json.isbn  == '9780547928228'
    }

    void "GET /books/create returns a new transient instance"() {
        when:
        request.method = 'GET'
        request.format = 'json'
        controller.create()

        then:
        response.status == HttpStatus.OK.value()
    }

    void "GET /books/{id}/edit returns the book for editing"() {
        given:
        Book b = new Book(title: 'Editable', isbn: '9780547928228', pageCount: 100)
            .save(flush: true, failOnError: true)

        when:
        request.method = 'GET'
        request.format = 'json'
        params.id = b.id
        controller.edit()

        then:
        response.status == HttpStatus.OK.value()
        response.json.title == 'Editable'
    }

    void "PUT /books/{id} updates the book"() {
        given:
        Book b = new Book(title: 'Original', isbn: '9780547928228', pageCount: 100)
            .save(flush: true, failOnError: true)

        when:
        request.method = 'PUT'
        request.json = [title: 'Updated']
        params.id = b.id
        controller.update()

        then:
        response.status == HttpStatus.OK.value()
        response.json.title == 'Updated'
    }

    void "PUT /books/{id} with invalid data returns 422"() {
        given:
        Book b = new Book(title: 'Original', isbn: '9780547928229', pageCount: 100)
            .save(flush: true, failOnError: true)

        when:
        request.method = 'PUT'
        request.json = [title: '']
        params.id = b.id
        controller.update()

        then:
        response.status == HttpStatus.UNPROCESSABLE_ENTITY.value()
    }

    void "DELETE /books/{id} deletes the book"() {
        given:
        Book b = new Book(title: 'Deletable', isbn: '9780547928228', pageCount: 100)
            .save(flush: true, failOnError: true)

        when:
        request.method = 'DELETE'
        params.id = b.id
        controller.delete()

        then:
        response.status == HttpStatus.NO_CONTENT.value()
        Book.get(b.id) == null
    }
}

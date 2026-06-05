package example.grails

import groovy.transform.CompileStatic

@CompileStatic
class BootStrap {

    BookGormService bookGormService

    def init = { servletContext ->
        [
                new Book(isbn: '1491950358', name: 'Building Microservices'),
                new Book(isbn: '1680502395', name: 'Release It!'),
                new Book(isbn: '0321601912', name: 'Continuous Delivery')
        ].each {book ->
            bookGormService.saveBook(book)
        }
    }
    def destroy = {
    }
}

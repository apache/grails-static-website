package example.grails

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
class BooksController {

    BookGormService bookGormService

    @Autowired
    AnalyticsClient analyticsClient

    static allowedMethods = [
            index: 'GET',
            show: 'GET'
    ]

    def index() {
        [books: bookGormService.findAll()]
    }

    def show(String isbn) {
        Book book = bookGormService.findByIsbn(isbn)
        if (!book) {
            response.status = 404
            return
        }
        analyticsClient.updateAnalytics([isbn: book.isbn])
        render(template: 'book', model: [book: book])
    }
}
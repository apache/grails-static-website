package customer

import example.Book
import example.BookService

/**
 * Customer webapp's read-only catalog.
 *
 * Reuses example.Book and example.BookService from the shared-core
 * plugin. Notice this controller does not extend RestfulController -
 * the customer side does not need create/update/delete; it only needs
 * to render the catalog as GSP pages with whatever filtering the UI
 * exposes.
 */
class CatalogController {

    static allowedMethods = [index: 'GET', show: 'GET']

    BookService bookService

    def index() {
        Integer max    = Math.min(params.int('max', 25), 100)
        Integer offset = params.int('offset', 0)
        respond bookService.list([max: max, offset: offset, sort: 'title']),
                model: [bookCount: bookService.count()]
    }

    def show(Long id) {
        Book book = bookService.get(id)
        if (!book) { response.status = 404; return }
        respond book
    }
}

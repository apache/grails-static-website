package admin

import example.Book
import example.BookService
import grails.rest.RestfulController

/**
 * Admin webapp's BookController.
 *
 * Reuses example.Book and example.BookService from the shared-core
 * plugin; this module only owns the controller. The package separation
 * (admin.* vs example.*) keeps app-specific UI concerns out of the
 * shared module.
 */
class BookController extends RestfulController<Book> {

    static responseFormats = ['html', 'json']

    BookService bookService

    BookController() { super(Book) }
}

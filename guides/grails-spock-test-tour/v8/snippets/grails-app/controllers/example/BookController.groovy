package example

import grails.rest.RestfulController

class BookController extends RestfulController<Book> {
    static responseFormats = ['json']
    BookService bookService
    BookController() { super(Book) }
}

package example

import grails.rest.RestfulController

class BookController extends RestfulController<Book> {
    static responseFormats = ['json']
    BookController() { super(Book) }

    @Override
    protected List<Book> listAllResources(Map params) {
        params.max = Math.min(params.int('max', 25), 100)
        Book.list(params)
    }
}

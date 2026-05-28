package example

import grails.rest.RestfulController

class BookController extends RestfulController<Book> {
    // 'json' keeps the REST CRUD surface; 'html' lets the index action render
    // grails-app/views/book/index.gsp so a browser (and the Geb functional
    // test) gets a real HTML table at /books.
    static responseFormats = ['json', 'html']
    BookService bookService
    BookController() { super(Book) }

    // Override index so the HTML format renders the GSP table view (through the
    // sitemesh layout) while JSON clients still get the REST collection.
    def index(Integer max) {
        params.max = Math.min(max ?: 100, 100)
        List<Book> books = listAllResources(params)
        withFormat {
            html { [bookList: books, bookCount: countResources()] }
            json { respond books, [model: [bookCount: countResources()]] }
        }
    }
}

package example

import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import org.springframework.http.HttpStatus

class BookController extends RestfulController<Book> {

    static responseFormats = ['json']

    BookController() { super(Book) }

    @Override
    protected List<Book> listAllResources(Map params) {
        params.max = Math.min(params.int('max', 25), 100)
        Book.list(params)
    }

    /**
     * POST /v1/books/bulk
     *
     * All-or-nothing bulk create. The whole request rolls back on the
     * first validation failure; the response is a structured 422 with
     * the field errors of every failing book in the batch.
     */
    @Transactional
    def bulkCreate() {
        if (!request.JSON instanceof List) {
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            respond errors: [[message: 'request body must be a JSON array of Book objects']]
            return
        }

        List<Book> drafts = request.JSON.collect { json -> new Book(json as Map) }
        List<Map> failures = []
        drafts.eachWithIndex { Book b, int i ->
            if (!b.validate()) {
                failures << [index: i, errors: b.errors.fieldErrors.collect {
                    [field: it.field, code: it.code, message: it.defaultMessage]
                }]
            }
        }

        if (failures) {
            transactionStatus.setRollbackOnly()
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            respond books: failures
            return
        }

        drafts*.save(flush: true)
        response.status = HttpStatus.CREATED.value()
        respond books: drafts
    }
}

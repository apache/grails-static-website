package example

import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError

class BookController extends RestfulController<Book> {

    static responseFormats = ['json']

    BookController() {
        super(Book)
    }

    @Override
    def index(Integer max) {
        if (max != null && max < 0) {
            max = null
        }

        params.max = Math.min(max ?: 25, 100)
        params.offset = Math.max(params.int('offset', 0), 0)

        respond listAllResources(params), model: [bookCount: countResources()]
    }

    @Override
    protected List<Book> listAllResources(Map params) {
        Long authorId = params.long('author')
        Map queryParams = [max: params.max, offset: params.offset, sort: params.sort, order: params.order].findAll { it.value != null }

        if (authorId != null) {
            return Book.where {
                author.id == authorId
            }.list(queryParams)
        }

        Book.list(queryParams)
    }

    @Override
    protected Integer countResources() {
        Long authorId = params.long('author')
        if (authorId != null) {
            return Book.where {
                author.id == authorId
            }.count() as Integer
        }

        Book.count()
    }

    @Transactional
    def bulkCreate() {
        if (!(request.JSON instanceof List)) {
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            respond([
                errors: [[code: 'invalid.body', message: 'Request body must be a JSON array of book objects']]
            ])
            return
        }

        List<Book> drafts = request.JSON.collect { Object json ->
            Book draft = new Book()
            bindData(draft, json as Map)
            draft
        }

        List<Map<String, Object>> failures = []
        drafts.eachWithIndex { Book draft, int index ->
            draft.validate()
            if (draft.hasErrors()) {
                failures << [
                    index : index,
                    errors: draft.errors.allErrors.collect { error ->
                        Map<String, Object> payload = [
                            object : error.objectName,
                            code   : error.code,
                            message: message(error: error)
                        ]
                        if (error instanceof FieldError) {
                            payload.field = error.field
                            payload.rejectedValue = error.rejectedValue
                            payload.bindingFailure = error.bindingFailure
                        }
                        payload
                    }
                ]
            }
        }

        if (failures) {
            transactionStatus.setRollbackOnly()
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            respond([books: failures])
            return
        }

        drafts.each { Book book ->
            book.save(flush: true, failOnError: true)
        }

        params.max = Math.max(drafts.size(), 1)
        params.offset = 0
        respond drafts, [status: HttpStatus.CREATED, view: 'index', model: [bookCount: drafts.size()]]
    }
}

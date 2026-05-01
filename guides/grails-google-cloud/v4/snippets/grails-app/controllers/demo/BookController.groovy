// tag::packageImport[]
package demo

import static org.springframework.http.HttpStatus.*
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.context.MessageSource

// end::packageImport[]
// tag::imports[]
// end::imports[]
// tag::classDeclaration[]
@SuppressWarnings('LineLength')
@CompileStatic
class BookController {
// end::classDeclaration[]

  // tag::properties[]
    static allowedMethods = [
            index: 'GET',
            show: 'GET',
            edit: 'GET',
            create: 'GET',
            save: 'POST',
            update: 'PUT',
            delete: 'DELETE',
    ]

    BookGormService bookGormService

    MessageSource messageSource
    // end::properties[]

    // tag::actionIndex[]
    @CompileDynamic
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def (l, total) = bookGormService.list(params)
        respond l, model: [bookCount: total]
    }
    // end::actionIndex[]

    // tag::actionShow[]
    @Transactional(readOnly = true)
    def show(Book book) {
        respond book
    }
    // end::actionShow[]

    // tag::actionCreate[]
    @SuppressWarnings(['FactoryMethodName', 'GrailsMassAssignment'])
    @Transactional(readOnly = true)
    def create() {
        respond new Book(params)
    }
    // end::actionCreate[]

    // tag::actionEdit[]
    @Transactional(readOnly = true)
    def edit(Book book) {
        respond book
    }
    // end::actionEdit[]

    // tag::actionSave[]
    @CompileDynamic
    def save(NameCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [book: cmd], view:'create'
            return
        }

        def book = bookGormService.save(cmd)

        if ( book == null ) {
            notFound()
            return
        }

        if ( book.hasErrors() ) {
            respond book.errors, model: [book: book], view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                String bookMessage = messageSource.getMessage('book.label', [] as Object[], 'Book', request.locale)
                String message = messageSource.getMessage('default.created.message', [bookMessage, book.id] as Object[], 'Book Created', request.locale)
                flash.message = message
                redirect book
            }
            '*' { respond book, [status: CREATED] }
        }
    }
    // end::actionSave[]

    // tag::actionUpdate[]
    @CompileDynamic
    def update(NameUpdateCommand cmd) {
        if (cmd == null) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, model: [book: cmd], view: 'edit'
            return
        }

        def book = bookGormService.update(cmd)

        if ( book == null) {
            notFound()
            return
        }

        if ( book.hasErrors() ) {
            respond book.errors, model: [book: book], view: 'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                String bookMessage = messageSource.getMessage('book.label', [] as Object[], 'Book', request.locale)
                String message = messageSource.getMessage('default.updated.message', [bookMessage, book.id] as Object[], 'Book Updated', request.locale)
                flash.message = message
                redirect book
            }
            '*' { respond book, [status: OK] }
        }
    }
    // end::actionUpdate[]

    // tag::actionDelete[]
    @CompileDynamic
    def delete() {

        Long bookId = params.long('id')

        if ( !bookId ) {
            notFound()
            return
        }

        bookGormService.deleteById(bookId)

        request.withFormat {
            form multipartForm {
                String bookMessage = messageSource.getMessage('book.label', [] as Object[], 'Book', request.locale)
                String message = messageSource.getMessage('default.deleted.message', [bookMessage, bookId] as Object[], 'Book Deleted', request.locale)
                flash.message = message
                redirect action: 'index', method: 'GET'
            }
            '*' { render status: NO_CONTENT }
        }
    }
    // end::actionDelete[]

    // tag::notFound[]
    @CompileDynamic
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                String bookMessage = messageSource.getMessage('book.label', [] as Object[], 'Book', request.locale)
                String message = messageSource.getMessage('default.not.found.message', [bookMessage, params.id] as Object[], 'Book not Found', request.locale)
                flash.message = message
                redirect action: 'index', method: 'GET'
            }
            '*' { render status: NOT_FOUND }
        }
    }
    // end::notFound[]
//tag::featuredImage[]
UploadBookFeaturedImageService uploadBookFeaturedImageService

@Transactional(readOnly = true)
def editFeaturedImage(Book book) {
     respond book
}
@CompileDynamic
def uploadFeaturedImage(FeaturedImageCommand cmd) {

    if (cmd.hasErrors()) {
         respond(cmd.errors, model: [book: cmd], view: 'editFeaturedImage')
        return
    }

    def book = uploadBookFeaturedImageService.uploadFeaturedImage(cmd)
    if (book == null) {
        notFound()
        return
    }

    if (book.hasErrors()) {
        respond(book.errors, model: [book: book], view: 'editFeaturedImage')
        return
    }

    request.withFormat {
        form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'book.label', default: 'Book'), book.id])
            redirect book
        }
        '*' { respond book, [status: OK] }
    }
}
//end::featuredImage[]
}

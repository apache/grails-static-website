package demo

import groovy.transform.CompileStatic
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@CompileStatic
class BookController {

    static allowedMethods = [
            index: 'GET',
            show: 'GET',
            delete: 'POST',
            undoDelete: 'POST',
    ]

    BookDataService bookDataService
    MessageSource messageSource

    def index() {
        [
                total: bookDataService.count(),
                bookList: bookDataService.findAll(),
                undoId: params.long('undoId')
        ]
    }

    def show(Long id) {
        [bookInstance: bookDataService.findById(id)]
    }

    def delete(Long id) {
        bookDataService.delete(id)
        flash.message = messageSource.getMessage('book.delete.undo',
                [id] as Object[],
                'Book deleted',
                LocaleContextHolder.locale
        )
        redirect(action: 'index', params: [undoId: id])
    }

    def undoDelete(Long id) {
        bookDataService.unDelete(id)
        flash.message = messageSource.getMessage('book.unDelete',
                [] as Object[],
                'Book restored',
                LocaleContextHolder.locale
        )
        redirect(action: 'index')
    }

}
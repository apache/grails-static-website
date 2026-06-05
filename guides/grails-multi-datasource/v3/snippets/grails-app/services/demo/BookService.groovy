package demo

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@CompileStatic
class BookService {

    @Transactional('books') // <1>
    Book addBook(String title, List<String> keywords) {
        Book book = new Book(title: title)
        if ( keywords ) {
            for ( String keyword : keywords ) {
                book.addToKeywords(new Keyword(name: keyword))
            }
        }
        if ( !book.save() ) {
            log.error 'Unable to save book'
        }
        book
    }
}
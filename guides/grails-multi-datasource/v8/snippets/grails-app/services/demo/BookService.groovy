package demo

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j

@Slf4j
class BookService {

    @Transactional('books') // <1>
    Book addBook(String title, List<String> keywords) {
        Book book = new Book(title: title)
        if (keywords) {
            for (String keyword : keywords) {
                Keyword keywordInstance = new Keyword(name: keyword)
                keywordInstance.books.save() // <2>
                book.addToKeywords(keywordInstance)
            }
        }
        if (!book.save()) {
            log.error 'Unable to save book'
        }
        book
    }
}

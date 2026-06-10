package example

import grails.gorm.transactions.Transactional
import java.time.LocalDate

@Transactional
class BookService {

    List<Book> listByAuthor(Author author, Integer max = 20) {
        Book.createCriteria().list(max: max) {
            eq 'author', author
            order 'title', 'asc'
        } as List<Book>
    }

    List<Book> findPublishedSince(LocalDate since) {
        if (!since) {
            return []
        }
        Book.where {
            publishedOn >= since
        }.list(sort: 'publishedOn', order: 'desc')
    }

    List<Book> searchByTitle(String term) {
        if (!term?.trim()) {
            return []
        }
        String pattern = "%${term.trim()}%"
        Book.createCriteria().list {
            ilike 'title', pattern
            order 'title'
        } as List<Book>
    }

    Book saveBook(Book book) {
        book.save(failOnError: true, flush: true)
    }
}

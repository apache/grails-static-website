package example

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@Transactional
class BookQueryService {

    /**
     * Criteria-style query: books for an author by name (case-insensitive contains).
     */
    @ReadOnly
    List<Book> findBooksByAuthorName(String authorName) {
        if (!authorName?.trim()) {
            return []
        }
        String pattern = "%${authorName.trim()}%"
        Book.createCriteria().list {
            author {
                ilike('name', pattern)
            }
            order('title')
        } as List<Book>
    }

    /**
     * HQL aggregate: how many books an author has published.
     */
    @ReadOnly
    long countBooksForAuthor(Long authorId) {
        if (!authorId) {
            return 0L
        }
        (Book.executeQuery(
            'select count(b) from Book b where b.author.id = :authorId',
            [authorId: authorId]
        )[0] as Long) ?: 0L
    }

    /**
     * Criteria-style query: books at or above a minimum price.
     */
    @ReadOnly
    List<Book> findBooksWithMinPrice(BigDecimal minimumPrice) {
        BigDecimal min = minimumPrice ?: BigDecimal.ZERO
        Book.where {
            price >= min
        }.list(sort: 'price', order: 'desc')
    }
}

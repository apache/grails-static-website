package demo

import grails.gorm.services.Query
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@CompileStatic
interface IBookPageViewDataService {
    void delete(Serializable id)

    BookPageView save(Long bookId, String bookName, Integer views)

    BookPageView findByBookId(Long bookId)

    @Query("select $b.views from ${BookPageView b} where $b.bookId = $bookIdParam") // <1>
    List<Integer> findViewsByBookId(Long bookIdParam)

    @Query("update ${BookPageView b} set ${b.views} = ${b.views} + 1 where $b.bookId = $bookIdParam") // <2>
    Number updateViews(Long bookIdParam)
}

@Service(BookPageView)
abstract class BookPageViewDataService implements IBookPageViewDataService {

    @Transactional
    void increment(Long bookId, String bookName) {
        List<Integer> views = findViewsByBookId(bookId)
        if (!views) {
            save(bookId, bookName, 1)
        } else {
            updateViews(bookId)
        }
    }
}
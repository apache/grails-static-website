package example.grails

import grails.gorm.services.Query
import grails.gorm.services.Service
import javax.inject.Singleton

@Singleton
@Service(BookAnalytics)
interface BookAnalyticsGormService {

    List<BookAnalytics> findAll()

    BookAnalytics findByIsbn(String isbn)

    BookAnalytics saveBookAnalytics(BookAnalytics bookAnalytics)

    @Query("update ${BookAnalytics bookAnalytics} set ${bookAnalytics.count} = $newCount where bookAnalytics.isbn = $isbn") // <1>
    void updateCount(String isbn, Long newCount)

}
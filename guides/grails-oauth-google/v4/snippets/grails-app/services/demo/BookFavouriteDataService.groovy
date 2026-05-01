package demo

import grails.gorm.services.Query
import grails.gorm.services.Service

@Service(BookFavourite)
interface BookFavouriteDataService {

    BookFavourite save(Long bookId, String username)

    @Query("select $b.bookId from ${BookFavourite b} where $b.username = $username") // <1>
    List<Long> findBookIdByUsername(String username)
}

package example

import grails.gorm.services.Service

@Service(Book)
interface BookService {

    Book   get(Serializable id)
    List<Book> list(Map args)
    Long   count()
    Book   save(Book book)
    Book   delete(Serializable id)

    Book   findByIsbn(String isbn)
    Long   countByPublishedOnGreaterThanEquals(Date threshold)
}

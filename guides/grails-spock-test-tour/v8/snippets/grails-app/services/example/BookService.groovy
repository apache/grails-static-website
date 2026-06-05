package example

import grails.gorm.services.Service

@Service(Book)
interface BookService {
    Book   get(Serializable id)
    Book   save(Book book)
    Long   countByPageCountGreaterThanEquals(Integer threshold)
    Book   findByIsbn(String isbn)
    List<Book> list(Map args)
}

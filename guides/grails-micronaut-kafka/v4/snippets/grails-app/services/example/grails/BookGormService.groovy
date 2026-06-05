package example.grails

import grails.gorm.services.Service

@Service(Book)
interface BookGormService {
    Book saveBook(Book book)

    List<Book> findAll()

    Book findByIsbn(String isbn)
}
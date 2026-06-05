package demo

import grails.gorm.services.Service
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

interface IBookDataService {

    Book save(String title, String author, String about, String href, String image)

    Number count()

    Book findById(Long id)

    void delete(Long id) // <1>
}

@Service(Book)
abstract class BookDataService implements IBookDataService {

    @ReadOnly
    List<BookImage> findAll() {  // <2>
        Book.where {}.projections {
            property('id')
            property('image')
        }.list().collect { new BookImage(id: it[0] as Long, image: it[1] as String) }
    }

    @Transactional
    void unDelete(Long id) {
        Book.withDeleted { // <3>
            Book book = Book.get(id)
            book?.unDelete() // <4>
        }
    }
}
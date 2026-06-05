package demo

import grails.gorm.services.Service
import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileDynamic
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.hibernate.transform.Transformers

interface IBookDataService {
    Book save(String title, String author, String about, String href, String image)
    Number count()
    Book findById(Long id)
}

@Service(Book)
abstract class BookDataService implements IBookDataService {

    @CompileDynamic
    @ReadOnly
    List<BookImage> findAll() {
        BuildableCriteria c = Book.createCriteria()
        c.list {
            resultTransformer(Transformers.aliasToBean(BookImage))
            projections {
                property('id', 'id')
                property('image', 'image')
            }
        }
    }

    @CompileDynamic
    @ReadOnly
    List<BookImage> findAllByIds(List<Long> ids) {
        BuildableCriteria c = Book.createCriteria()
        c.list {
            inList('id', ids)
            resultTransformer(Transformers.aliasToBean(BookImage))
            projections {
                property('id', 'id')
                property('image', 'image')
            }
        }
    }
}


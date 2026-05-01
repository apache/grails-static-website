package demo

import grails.gorm.services.Service
import groovy.transform.CompileStatic

@CompileStatic
@Service(Book)
interface BookDataService {

    Book save(String title, String author, Integer pages)

    List<Book> findAll()

    Book update(Serializable id, String title)

    void delete(Serializable id)
}
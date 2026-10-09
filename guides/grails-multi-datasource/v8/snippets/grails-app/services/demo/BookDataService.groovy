package demo

import grails.gorm.services.Join
import grails.gorm.services.Service
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@CompileStatic
@Service(Book)
interface BookDataService {

    @Join('keywords') // <2>
    @ReadOnly('books') // <1>
    List<Book> findAll()

    @Transactional('books') // <1>
    void deleteByTitle(String title)
}

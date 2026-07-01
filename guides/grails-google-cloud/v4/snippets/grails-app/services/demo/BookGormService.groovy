package demo

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class BookGormService {

    // tag::list[]
    @Transactional(readOnly = true)
    List list(Map params) {
        [Book.list(params), Book.count()]
    }
    // end::list[]

    // tag::save[]
    @CompileDynamic
    Book save(NameCommand cmd) {
        def book = new Book()
        book.properties = cmd.properties
        book.save()
    }
    // end::save[]

    // tag::update[]
    @CompileDynamic
    Book update(NameUpdateCommand cmd) {
        Book book = Book.get(cmd.id)
        book.properties = cmd.properties
        book.save()
    }
    // end::update[]

    // tag::deleteById[]
    void deleteById(Long bookId) {
        def book = Book.get(bookId)
        book?.delete()
    }
    // end::deleteById[]

//tag::updateFeaturedImageUrl[]
@SuppressWarnings('LineLength')
Book updateFeaturedImageUrl(Long id, Long version, String fileName, String featuredImageUrl, boolean flush = false) {
    Book book = Book.get(id)
    if ( !book ) {
        return null
    }
    book.version = version
    book.fileName = fileName
    book.featuredImageUrl = featuredImageUrl
    book.save(flush: flush)
    book
}
//end::updateFeaturedImageUrl[]
}

package demo

import grails.gorm.transactions.Rollback
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.testing.GrailsUnitTest
import org.springframework.transaction.PlatformTransactionManager
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class TitleListenerServiceGrailsUnitSpec extends Specification implements GrailsUnitTest { // <1>

    @Shared
    @AutoCleanup
    HibernateDatastore hibernateDatastore //<2>

    @Shared
    PlatformTransactionManager transactionManager

    void setupSpec() {
        hibernateDatastore = applicationContext.getBean(HibernateDatastore) //<2>
        transactionManager = hibernateDatastore.getTransactionManager()
    }

    @Override
    Closure doWithSpring() { //<3>
        { ->
            friendlyUrlService(FriendlyUrlService)
            serialNumberGeneratorService(SerialNumberGeneratorService)
            titleListenerService(TitleListenerService) {
                friendlyUrlService = ref('friendlyUrlService')
                serialNumberGeneratorService = ref('serialNumberGeneratorService')
            }
            datastore(HibernateDatastore, [Book])
        }
    }

    @Rollback
    def "serialNumber and friendyUrl are populated after book is saved"() { //<4>

        when:
        Book book = new Book(title: 'Practical Grails 3', author: 'Eric Helgeson', pages: 100)
        book.save(flush: true)

        then:
        !book.hasErrors()

        when:
        book = Book.findByTitle('Practical Grails 3')

        String serialNumber = book.serialNumber
        String friendlyUrl = book.friendlyUrl

        then:
        serialNumber
        friendlyUrl == 'practical-grails-3'

        when: 'updating book title'
        book.title = 'Grails 3'
        book.save(flush: true)

        then:
        !book.hasErrors()

        when:
        book = Book.findByTitle('Grails 3')

        then: 'serial number stays the same'
        serialNumber == book.serialNumber

        and: 'friendly url changes'
        friendlyUrl != book.friendlyUrl
        book.friendlyUrl == 'grails-3'
    }
}
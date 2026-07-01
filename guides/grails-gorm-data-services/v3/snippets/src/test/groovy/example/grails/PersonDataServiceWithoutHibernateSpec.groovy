//tag::packagedeclaration[]
package example.grails

import grails.gorm.transactions.Transactional

//end::packagedeclaration[]

//tag::imports[]
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.PlatformTransactionManager
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
//end::imports[]

//tag::classContent[]
@Transactional
class PersonDataServiceWithoutHibernateSpec extends Specification {

    @Shared // <2>
    PersonDataService personDataService

    @Shared // <2>
    @AutoCleanup // <3>
    HibernateDatastore hibernateDatastore

    @Shared // <2>
    PlatformTransactionManager transactionManager

    void setupSpec() {
        hibernateDatastore = new HibernateDatastore(Person) // <4>
        transactionManager = hibernateDatastore.getTransactionManager() // <5>
        personDataService = this.hibernateDatastore.getService(PersonDataService)
    }

    @Rollback // <6>
    void "test find person by name"() {
        given:
        Person p = new Person(name: "Nirav", age: 39).save()

        when:
        Person person = personDataService.findByName("Nirav")

        then:
        person.name == "Nirav"
        person.age == 39
    }
    //end::classContent[]
}

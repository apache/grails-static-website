//tag::packagedeclaration[]
package example.grails
//end::packagedeclaration[]

//tag::imports[]
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.PlatformTransactionManager
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
//end::imports[]

class PersonDataServiceWithoutHibernateSpec extends Specification {

    //tag::classContent[]
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

    @Rollback // <5>
    void "test find person by name"() {
        given:
        Person p = personDataService.save("Nirav", 39)

        when:
        Person person = personDataService.findByName("Nirav")

        then:
        person.name == "Nirav"
        person.age == 39

        cleanup:
        personDataService.delete(p.id) // <6>
    }
    //end::classContent[]
}

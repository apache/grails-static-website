// tag::PersonDataServiceSpec[]
package example.grails

import grails.test.hibernate.HibernateSpec
import spock.lang.Shared

class PersonDataServiceSpec extends HibernateSpec {

    @Shared
    PersonDataService personDataService

    def setup() {
        personDataService = hibernateDatastore.getService(PersonDataService)
    }

    void "test find person by name"() {
        given:
        personDataService.save("Nirav", 39)

        when:
        Person person = personDataService.findByName("Nirav")

        then:
        person.name == "Nirav"
        person.age == 39

        cleanup:
        personDataService.delete(person.id)
    }
// end::PersonDataServiceSpec[]

    // tag::test1[]
    void "test find persons age projection"() {
        given:
        Person person = personDataService.save("Nirav", 39)

        when:
        Integer age = personDataService.findPersonAge("Nirav")

        then:
        age == 39

        cleanup:
        personDataService.delete(person.id)
    }
    // end::test1[]

    // tag::test2[]
    void "test save person"() {
        when:
        Person person = personDataService.save("Bob", 22)

        then:
        person.name == "Bob"
        person.age == 22
        personDataService.count() == old(personDataService.count()) + 1 // <1>

        cleanup:
        personDataService.delete(person.id)
    }
    // end::test2[]

    // tag::test3[]
    void "test save person with address"() {
        when:
        Person person = personDataService.saveWithListOfAddressesMap("Bob", 22, [
                [streetName:  "500 Berry St", city: "Fort Worth", state: "TX", country: "USA"]
        ])

        then:
        person.name == "Bob"
        person.addresses[0].streetName == "500 Berry St"
        person.addresses[0].city == "Fort Worth"
        person.addresses[0].state == "TX"

        cleanup:
        personDataService.delete(person.id)
    }
    // end::test3[]
}

//tag::clazz[]
package example.grails

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration // <1>
class PersonDataServiceIntSpec extends Specification {

    PersonDataService personDataService // <2>
//end::clazz[]
    //tag::findEagerly[]
    void "test join eager load"() {
        given:
        Person p = personDataService.saveWithListOfAddressesMap('Nirav',39, [
                [streetName: "101 Main St", city: "Grapevine", state: "TX", country: "USA"],
                [streetName: "2929 Pearl St", city: "Austin", state: "TX", country: "USA"],
                [streetName: "21 Sewickly Hills Dr", city: "Sewickley", state: "PA", country: "USA"]
        ])

        when:
        Person person = personDataService.findEagerly("Nirav")

        then:
        person.name == "Nirav"
        person.age == 39

        when:
        List<String> cities = person.addresses*.city

        then:
        cities.contains("Grapevine")
        cities.contains("Austin")
        cities.contains("Sewickley")

        cleanup:
        personDataService.delete(p.id)

    }
    //end::findEagerly[]

    //tag::findAllByCountry[]
    void "test search persons by country"() {
        given:
        [
                [name: 'Nirav', age: 39, addresses: [
                        [streetName: "101 Main St", city: "Grapevine", state: "TX", country: "USA"],
                        [streetName: "2929 Pearl St", city: "Austin", state: "TX", country: "USA"],
                        [streetName: "21 Sewickly Hills Dr", city: "Sewickley", state: "PA", country: "USA"]]],
                [name: 'Jeff', age: 50, addresses: [
                        [streetName: "888 Olive St", city: "St Louis", state: "MO", country: "USA"],
                        [streetName: "1515 MLK Blvd", city: "Austin", state: "TX", country: "USA"]]],
                [name: 'Sergio', age: 35, addresses: [
                        [streetName: "19001 Calle Mayor", city: "Guadalajara", state: 'Castilla-La Mancha', country: "Spain"]]]
        ].each { Map<String, Object> m ->
            personDataService.saveWithListOfAddressesMap(m.name as String,
                    m.age as Integer,
                    m.addresses as List<Map<String, Object>>)
        }

        when:
        List<Person> usaPersons = personDataService.findAllByCountry("USA")

        then:
        usaPersons
        usaPersons.size() == 2
        usaPersons.find { it.name == "Nirav"}
        usaPersons.find { it.name == "Jeff"}

        when:
        List<Person> spainPersons = personDataService.findAllByCountry("Spain")

        then:
        spainPersons.size() == 1
        spainPersons.find { it.name == "Sergio"}
    }
    //end::findAllByCountry[]
}

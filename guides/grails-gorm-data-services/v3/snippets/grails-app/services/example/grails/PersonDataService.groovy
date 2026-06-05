//tag::packageDeclaration[]
package example.grails

import grails.gorm.services.Query

//end::packageDeclaration[]

//tag::serviceImport[]
import grails.gorm.services.Service
//end::serviceImport[]

//tag::joinImport[]
import grails.gorm.services.Join
//end::joinImport[]

//tag::miscImports[]
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
//end::miscImports[]

//tag::IPersonDataService[]
interface IPersonDataService {
//end::IPersonDataService[]

    //tag::findByName[]
    Person findByName(String name) // <1>
    //end::findByName[]

    //tag::findPersonAge[]
    Integer findPersonAge(String name)
    //end::findPersonAge[]

    //tag::countPerson[]
    Number count()
    //end::countPerson[]

    //tag::findEagerly[]
    @Join('addresses') // <1>
    Person findEagerly(String name)
    //end::findEagerly[]

    //tag::delete[]
    void delete(Serializable id) // <3>
    //end::delete[]

    //tag::save[]
    Person save(String name, Integer age)
    //end::save[]

    //tag::savePerson[]
    Person save(Person person)
    //end::savePerson[]

    //tag::findAllByCountry[]
    // <1>
    @Query("""\
select distinct ${p} 
from ${Person p} join ${p.addresses} a 
where a.country = $country
""") // <2>
    List<Person> findAllByCountry(String country) // <3>
    //end::findAllByCountry[]
}

//tag::abstractClass[]
@Service(Person)
abstract class PersonDataService implements IPersonDataService {

    @Transactional
    Person saveWithListOfAddressesMap(String name, Integer age, List<Map<String, Object>> addresses) {
        saveWithAddresses(name, age, addresses.collect { Map m ->
            new Address(streetName:  m.streetName as String,
                city:  m.city as String,
                state: m.state as String,
                country: m.country as String)
        } as List<Address>)
    }

    @Transactional
    Person saveWithAddresses(String name, Integer age, List<Address> addresses) {
        Person person = new Person(name: name, age: age)
        addresses.each { Address address ->
            person.addToAddresses(address)
        }
        save(person)
    }
}
//end::abstractClass[]

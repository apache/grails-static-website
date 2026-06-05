package example.grails

import grails.test.hibernate.HibernateSpec
import spock.lang.Shared

class AddressDataServiceSpec extends HibernateSpec {
    @Shared
    AddressDataService addressDataService

    @Shared
    PersonDataService personDataService

    def setup() {
        addressDataService = hibernateDatastore.getService(AddressDataService)
        personDataService = hibernateDatastore.getService(PersonDataService)
    }

    def "projections to POGO work"() {
        given:
        List<Long> personIds = []
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
            personIds << personDataService.saveWithListOfAddressesMap(m.name as String,
                    m.age as Integer,
                    m.addresses as List<Map<String, Object>>).id
        }

        when:
        List<Country> countries = addressDataService.findCountries()

        then:
        countries
        countries.size() == 2
        countries.any { it.name == 'USA' }
        countries.any { it.name == 'Spain' }

        cleanup:
        personIds.each {
            personDataService.delete(it)
        }
    }
}

package example.grails

import grails.gorm.services.Query
import grails.gorm.services.Service

@Service(Address)
interface AddressDataService {

    @Query("select new example.grails.Country(${a.country}) from ${Address a} group by ${a.country}") // <1>
    List<Country> findCountries()

}
package demo

import grails.gorm.services.Service
import grails.gorm.transactions.ReadOnly

@Service(Person)
interface PersonGormService {

    @ReadOnly
    List<Person> findAllByActive()
}
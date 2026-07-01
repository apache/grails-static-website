package example.grails

import grails.gorm.services.Service

@Service(Person)
interface PersonDataService {
    Person findPerson(String name)
    Person savePerson(String name, Integer age)
}
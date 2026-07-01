package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PersonSpec extends Specification implements DomainUnitTest<Person> {

    void 'name is required'() {
        when:
        def person = new Person()

        then:
        !person.validate()
        person.errors['name']
    }

    void 'age is optional'() {
        when:
        def person = new Person(name: 'Ada')

        then:
        person.validate()
    }
}

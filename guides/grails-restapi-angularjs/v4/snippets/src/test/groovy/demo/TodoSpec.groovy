package demo

import spock.lang.Specification
import grails.testing.gorm.DomainUnitTest

class TodoSpec extends Specification implements DomainUnitTest<Todo> {

    void "test name is required"() {
        when:
        def todo = new Todo(name: name)

        then:
        !todo.validate()
        todo.errors['name'].code == errorCode

        where:
        name | errorCode
        null | 'nullable'
        ''   | 'nullable'
    }
}

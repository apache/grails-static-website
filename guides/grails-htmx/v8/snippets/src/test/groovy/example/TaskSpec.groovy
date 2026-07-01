package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * Domain unit tests for Task - constraints and defaults, no Spring context.
 */
class TaskSpec extends Specification implements DomainUnitTest<Task> {

    void "title is required"() {
        when:
        Task t = new Task()

        then:
        !t.validate()
        t.errors.getFieldError('title').code == 'nullable'
    }

    void "a blank title is rejected"() {
        when: 'a blank string is assigned directly (the map constructor would null it)'
        Task t = new Task()
        t.title = ''

        then:
        !t.validate()
        t.errors.getFieldError('title').code == 'blank'
    }

    void "a title longer than maxSize is rejected"() {
        when:
        Task t = new Task(title: 'x' * 256)

        then:
        !t.validate()
        t.errors.getFieldError('title').code == 'maxSize.exceeded'
    }

    void "a new task defaults to not done"() {
        expect:
        !new Task(title: 'Something').done
    }

    void "a valid task validates"() {
        expect:
        new Task(title: 'Buy milk').validate()
    }
}

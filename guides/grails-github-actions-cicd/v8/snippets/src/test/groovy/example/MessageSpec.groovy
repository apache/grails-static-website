package example

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * Unit layer - runs in the `unit-test` CI stage (./gradlew test). No Spring
 * context, no database; just the domain constraints.
 */
class MessageSpec extends Specification implements DomainUnitTest<Message> {

    void "content is required"() {
        when:
        Message m = new Message()

        then:
        !m.validate()
        m.errors.getFieldError('content').code == 'nullable'
    }

    void "content over maxSize is rejected"() {
        when:
        Message m = new Message(content: 'x' * 501)

        then:
        !m.validate()
        m.errors.getFieldError('content').code == 'maxSize.exceeded'
    }

    void "a short message validates"() {
        expect:
        new Message(content: 'hello').validate()
    }

    void "toString returns the message content"() {
        expect:
        new Message(content: 'display me').toString() == 'display me'
    }
}

package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * Integration layer - runs in the `integration-test` CI stage
 * (./gradlew integrationTest) against a real PostgreSQL. @Rollback keeps
 * each method isolated and gives a bare GORM call an ambient session.
 */
@Integration
@Rollback
class MessageIntegrationSpec extends Specification {

    void "a message round-trips through GORM"() {
        given:
        Message saved = new Message(content: 'persist me').save(flush: true, failOnError: true)

        when:
        Message reloaded = Message.get(saved.id)

        then:
        reloaded.content == 'persist me'
    }

    void "a blank message is rejected by the database layer"() {
        when:
        Message m = new Message(content: '')
        m.save(flush: true)

        then:
        m.hasErrors()
        Message.get(m.id) == null
    }

    void "toString returns the message content"() {
        given:
        Message m = new Message(content: 'hello world').save(flush: true, failOnError: true)

        expect:
        m.toString() == 'hello world'
    }

    void "a message exceeding maxSize is rejected"() {
        when:
        Message m = new Message(content: 'x' * 501)
        m.save(flush: true)

        then:
        m.hasErrors()
        m.errors.getFieldError('content').code == 'maxSize.exceeded' || m.errors.getFieldError('content').code == 'maxSize'
    }
}

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
}

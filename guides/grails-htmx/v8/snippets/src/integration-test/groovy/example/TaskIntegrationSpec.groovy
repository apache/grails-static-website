package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

/**
 * @Integration boots the full context against the in-memory H2 database.
 * @Rollback isolates each method and provides an ambient session, so the
 * GORM queries the TaskController relies on can be exercised directly.
 */
@Integration
@Rollback
class TaskIntegrationSpec extends Specification {

    void "findAllByTitleIlike matches case-insensitively (the live-search query)"() {
        given:
        new Task(title: 'Buy Milk').save(flush: true, failOnError: true)
        new Task(title: 'Walk the dog').save(flush: true, failOnError: true)

        expect:
        Task.findAllByTitleIlike('%milk%')*.title == ['Buy Milk']
        Task.findAllByTitleIlike('%MILK%')*.title == ['Buy Milk']
        Task.findAllByTitleIlike('%walk%')*.title == ['Walk the dog']
    }

    void "tasks list newest first per the domain mapping"() {
        given:
        new Task(title: 'Older').save(flush: true, failOnError: true)
        Thread.sleep(20)
        new Task(title: 'Newer').save(flush: true, failOnError: true)

        expect:
        Task.list().first().title == 'Newer'
    }

    void "toggling done persists"() {
        given:
        Task t = new Task(title: 'Toggle').save(flush: true, failOnError: true)

        when:
        t.done = true
        t.save(flush: true)

        then:
        Task.get(t.id).done
    }
}

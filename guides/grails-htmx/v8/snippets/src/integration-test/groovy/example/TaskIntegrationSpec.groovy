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
        given: 'two tasks with explicit, distinct creation timestamps (no wall-clock sleep)'
        Task older = new Task(title: 'Older').save(flush: true, failOnError: true)
        Task newer = new Task(title: 'Newer').save(flush: true, failOnError: true)
        // dateCreated is auto-assigned on insert; autoTimestamp does not touch it on
        // update, so overriding it here gives the dateCreated-desc mapping a stable order.
        older.dateCreated = new Date(1_000_000L)
        newer.dateCreated = new Date(2_000_000L)
        older.save(flush: true, failOnError: true)
        newer.save(flush: true, failOnError: true)

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

    void "get returns null for non-existent id"() {
        expect:
        Task.get(999) == null
    }

    void "delete removes the entity from the database"() {
        given:
        Task t = new Task(title: 'Delete me').save(flush: true, failOnError: true)

        when:
        t.delete(flush: true)

        then:
        Task.get(t.id) == null
    }

    void "blank title is rejected by validation"() {
        when:
        new Task(title: '').save(flush: true, failOnError: true)

        then:
        thrown(grails.validation.ValidationException)
    }

    void "null title is rejected by validation"() {
        when:
        new Task(title: null).save(flush: true, failOnError: true)

        then:
        thrown(grails.validation.ValidationException)
    }

    void "search matches titles that contain punctuation characters"() {
        given:
        new Task(title: 'Foo (bar)').save(flush: true, failOnError: true)

        expect:
        Task.findAllByTitleIlike('%foo%')*.title == ['Foo (bar)']
        Task.findAllByTitleIlike('%(bar)%')*.title == ['Foo (bar)']
    }
}

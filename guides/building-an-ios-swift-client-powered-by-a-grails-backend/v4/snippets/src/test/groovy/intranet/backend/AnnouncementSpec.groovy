package intranet.backend

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
class AnnouncementSpec extends Specification implements DomainUnitTest<Announcement> {

    void "test body can be null"() {
        expect:
        new Announcement(body: null).validate(['body'])
    }

    void "test title can not be null"() {
        expect:
        !new Announcement(title: null).validate(['title'])
    }

    void "test body can have a more than 255 characters"() {

        when: 'for a string of 256 characters'
        String str = ''
        256.times { str += 'a' }

        then: 'body validation passes'
        new Announcement(body: str).validate(['body'])
    }

    void "test title can have a maximum of 255 characters"() {

        when: 'for a string of 256 characters'
        String str = ''
        256.times { str += 'a' }

        then: 'title validation fails'
        !new Announcement(title: str).validate(['title'])

        when: 'for a string of 256 characters'
        str = ''
        255.times { str += 'a' }

        then: 'title validation passes'
        new Announcement(title: str).validate(['title'])
    }
}

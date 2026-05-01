package intranet.backend

import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest

class AnnouncementServiceSpec extends HibernateSpec implements ServiceUnitTest<AnnouncementService> {

    def "test criteria query with projection returns a list of maps"() {

        when: 'Save some announcements'
        [new Announcement(title: 'Grails Quickcast #1: Grails Interceptors'),
        new Announcement(title: 'Grails Quickcast #2: JSON Views'),
        new Announcement(title: 'Grails Quickcast #3: Multi-Project Builds'),
        new Announcement(title: 'Grails Quickcast #4: Angular Scaffolding'),
        new Announcement(title: 'Retrieving Runtime Config Values In Grails 3'),
        new Announcement(title: 'Developing Grails 3 Applications With IntelliJ IDEA')].each {
            it.save()
        }

        then: 'announcements are saved'
        Announcement.count() == 6

        when: 'fetching the projection'
        def resp = service.findAllIdAndTitleProjections([:])

        then: 'there are six maps in the response'
        resp
        resp.size() == 6

        and: 'the maps contain only id and title'
        resp.each {
            it.keySet() == ['title', 'id'] as Set<String>
         }

        and: 'non empty values'
        resp.each {
            assert it.title
            assert it.id
        }

    }
}

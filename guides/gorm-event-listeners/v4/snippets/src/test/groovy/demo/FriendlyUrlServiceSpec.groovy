package demo

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class FriendlyUrlServiceSpec extends Specification implements ServiceUnitTest<FriendlyUrlService> {

    @Unroll
    def "Friendly Url for #title : #expected"(String title, String expected) {
        expect:
        expected == service.sanitizeWithDashes(title)

        where:
        title                | expected
        'Practical Grails 3' | 'practical-grails-3'
    }
}

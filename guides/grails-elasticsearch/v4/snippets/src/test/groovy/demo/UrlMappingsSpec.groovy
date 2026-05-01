package demo

import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification

class UrlMappingsSpec extends Specification implements UrlMappingsUnitTest<UrlMappings> {

    void setup() {
        mockController(BookController)
    }

    void "book/search endpoint mapping includes query term in path"() {
        expect:
        verifyForwardUrlMapping("/book/search/grails", controller: 'book', action: 'search') {
            q = 'grails'
        }
    }
}
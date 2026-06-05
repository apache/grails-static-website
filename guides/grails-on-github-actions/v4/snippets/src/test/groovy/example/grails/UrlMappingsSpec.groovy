package example.grails

import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification

class UrlMappingsSpec extends Specification implements UrlMappingsUnitTest<UrlMappings> {

    void "test forward mappings"() {
        expect:
        verifyForwardUrlMapping("/", view: 'index')
    }
}

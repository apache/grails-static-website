package demo

import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification

class ConferenceUrlMappingsSpec extends Specification implements UrlMappingsUnitTest<ConferenceUrlMappings> { //<1>

    void setup() {
        mockController(ConferenceController) //<2>
    }

    void "test conference mappings"() {

        expect: "calls to /conference/talks to succeed"
        verifyUrlMapping("/conf/talks", controller: 'conference', action: 'talks', method: 'GET')  //<3>
        verifyUrlMapping("/conf/talks/1", controller: 'conference', action: 'talks', method: 'GET') {
            id = '1'
        }

        when: "calling /conf/speakers"
        assertUrlMapping("/conf/speakers", controller: 'conference', action: 'speakers', method: 'GET') //<4>

        then: "no exception is thrown"
        noExceptionThrown()

    }

}

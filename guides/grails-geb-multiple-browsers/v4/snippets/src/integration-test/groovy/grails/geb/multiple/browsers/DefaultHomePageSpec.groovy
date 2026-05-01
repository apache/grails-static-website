package grails.geb.multiple.browsers

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@SuppressWarnings('JUnitPublicNonTestMethod')
@SuppressWarnings('MethodName')
@Integration
class DefaultHomePageSpec extends GebSpec {

    void 'verifies there is _<h1>_ header with the text _Welcome to Grails when we visit the home page.'() {
        when:
        go '/'

        then:

        $('h1').text() == 'Welcome to Grails'

    }
}

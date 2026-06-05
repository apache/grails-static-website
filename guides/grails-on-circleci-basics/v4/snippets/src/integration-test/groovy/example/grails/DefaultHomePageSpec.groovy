package example.grails

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class DefaultHomePageSpec extends GebSpec {

    def 'verifies there is _<h1>_ header with the text _Welcome to Grails_ when we visit the home page.'() {
        when:
        go '/'

        then:

        $('h1').text() == 'Welcome to Grails'

    }
}
package example.grails

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class DefaultHomePageSpec extends GebSpec {

    def 'verifies there is _<h1>_ header with the text _Welcome to Grails when we visit the home page.'() {
        when:
        HomePage page = to HomePage

        then:
        page.title == 'Welcome to Grails'

    }
}
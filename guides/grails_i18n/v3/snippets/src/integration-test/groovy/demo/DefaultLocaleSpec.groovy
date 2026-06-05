package demo

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class DefaultLocaleSpec extends GebSpec {

    def "change locale"() {
        when:
        go '/'

        then:
        $('h1').text() == 'Bienvenido a Grails'
    }
}

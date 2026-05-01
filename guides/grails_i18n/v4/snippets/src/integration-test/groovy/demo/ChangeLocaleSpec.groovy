package demo

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class ChangeLocaleSpec extends GebSpec {

    def "change locale"() {
        when:
        go '/?lang=en'

        then:
        $('h1').text() == 'Welcome to Grails'

        when:
        go '/?lang=es'

        then:
        $('h1').text() == 'Bienvenido a Grails'
    }
}

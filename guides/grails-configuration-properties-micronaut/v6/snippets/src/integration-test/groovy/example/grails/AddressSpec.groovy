package example.grails

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class AddressSpec extends GebSpec {

    def "verify address is displayed in homepage"() {
        when:
        browser.go("/")

        then:
        browser.driver.pageSource.contains('221B Baker Street')
    }
}

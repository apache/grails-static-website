package example.grails

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf

@Integration
class AddressSpec extends GebSpec {

    @IgnoreIf({ !sys['geb.env'] })
    def "verify address is displayed in homepage"() {
        when:
        browser.go("/")

        then:
        browser.driver.pageSource.contains('221B Baker Street')
    }
}

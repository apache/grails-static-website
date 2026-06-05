package example.grails

import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class AddressTagLibSpec extends Specification implements TagLibUnitTest<AddressTagLib> { // <1>

    Closure doWithSpring() {{ -> // <2>
        addressConfiguration(AddressConfiguration) {
            street = '221B Baker Street'
            city = 'London'
            country = 'United Kingdom'
        }
    }}

    void "test address tag"() {
        when:
        String returnString = tagLib.address().toString() // <3>

        then:
         returnString == "<div class='adr'>\n    <div class='street-address'>221B Baker Street</div>\n    <span class='locality'>London</span>,\n    <div class='country-name'>United Kingdom</div>\n</div>"
    }
}

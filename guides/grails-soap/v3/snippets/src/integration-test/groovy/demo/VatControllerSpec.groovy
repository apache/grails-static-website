package demo

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration
import spock.lang.Unroll

@Integration
class VatControllerSpec extends GebSpec {

    @Unroll
    def "#memberState : #vatNumber validation #unrollDescription when you submit the form"(String memberState, String vatNumber, Boolean expected, String unrollDescription) {

        when:
        VatFormPage page = browser.to VatFormPage
        page.validate(memberState, vatNumber)

        then:
        if ( expected) {
            browser.driver.pageSource.contains("${memberState} : ${vatNumber} is valid")
        } else {
            browser.driver.pageSource.contains("${memberState} : ${vatNumber} is NOT valid")
        }

        where:
        memberState | vatNumber   || expected
        'ES'        | 'B99286353' || true
        'ES'        | 'B19280031' || true
        'ES'        | 'XXXXXXXXX' || false

        unrollDescription = expected ? 'is successful' : ' fails'
    }
}

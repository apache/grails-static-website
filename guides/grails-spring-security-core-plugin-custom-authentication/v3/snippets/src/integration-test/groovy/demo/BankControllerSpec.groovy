package demo

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class BankControllerSpec extends GebSpec {

    def 'test bank controller is secured'() {
        when:
        baseUrl = "http://localhost:${serverPort}/"
        go 'bank'

        then:
        at LoginPage

        when:
        login('sherlock', 'elementary', BootStrap.BANKCARD[position()])

        then:
        driver.pageSource.contains('Welcome to your bank')
    }
}

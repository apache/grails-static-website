package example

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

/**
 * Functional layer - runs in the `functional-test` CI stage
 * (./gradlew functionalTest). ContainerGebSpec starts a Selenium-Chrome
 * container via Testcontainers and drives the booted application; the only
 * host requirement is a running Docker daemon.
 */
@Integration
class HomePageFunctionalSpec extends ContainerGebSpec {

    void 'the home page renders the welcome title'() {
        when: 'visiting the home page'
        go '/'

        then: 'the page title is correct'
        title == 'Welcome to Grails'
    }

    void 'a non-existent page renders a 404 response'() {
        when: 'visiting a non-existent URL'
        go '/doesNotExist'

        then: 'the 404 page is displayed'
        title == 'Page Not Found'
        $('h1').text().contains('Page Not Found (404)')
    }
}

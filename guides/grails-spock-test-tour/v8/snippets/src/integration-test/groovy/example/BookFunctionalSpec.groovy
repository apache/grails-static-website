package example

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

/**
 * Functional test driven by Geb 8 against a real booted application.
 *
 * ContainerGebSpec (from `testFixtures("org.apache.grails:grails-geb")`)
 * starts a Selenium-Chrome container via Testcontainers and points the
 * browser at the host-side Grails app. No local WebDriver binaries are
 * installed; the only host requirement is a running Docker daemon.
 *
 * @Integration is mandatory: GrailsContainerGebExtension throws at
 * runtime if the annotation is missing. @Rollback is NOT used here -
 * functional tests exercise the full HTTP stack and need committed
 * data.
 */
@Integration
class BookFunctionalSpec extends ContainerGebSpec {

    void "the book index page renders the seeded books"() {
        when:
        go '/books'

        then:
        title.contains('Book')
        $('table tbody tr').size() > 0
    }
}

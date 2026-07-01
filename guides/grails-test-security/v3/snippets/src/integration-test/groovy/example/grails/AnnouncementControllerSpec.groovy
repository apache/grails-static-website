package example.grails

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@SuppressWarnings('MethodName')
@Integration
class AnnouncementControllerSpec extends GebSpec {

    void 'test /announcement/index is secured, but accesible to users with role ROLE_BOSS'() {
        when: 'try to visit announcement listing without login'
        go '/announcement/index'

        then: 'it is redirected to login page'
        at LoginPage

        when: 'signs in with a ROLE_BOSS user'
        LoginPage page = browser.page(LoginPage)
        page.login('sherlock', 'elementary')

        then: 'he gets access to the announcement listing page'
        at AnnouncementListingPage
    }

    void 'test /announcement/index is secured, but accesible to users with role ROLE_EMPLOYEE'() {
        when: 'try to visit announcement listing without login'
        go '/announcement/index'

        then: 'it is redirected to login page'
        at LoginPage

        when: 'signs in with a ROLE_EMPLOYEE user'
        LoginPage page = browser.page(LoginPage)
        page.login('watson', '221Bbakerstreet')

        then: 'he gets access to the announcement listing page'
        at AnnouncementListingPage
    }
}

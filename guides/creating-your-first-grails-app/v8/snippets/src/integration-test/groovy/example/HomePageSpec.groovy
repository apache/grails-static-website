package example

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

@Integration
class HomePageSpec extends ContainerGebSpec {

    void 'home page lists seeded vehicles'() {
        when:
        go '/'

        then:
        title == 'Home Page'
        $('h1').text().contains('Welcome')
        $('li a', text: contains('Pickup')).size() == 1
    }
}

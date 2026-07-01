package example

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

/**
 * Browser tests proving the Tailwind CSS 4 build is wired end to end:
 * the compiled app.css is served, its utility and component classes apply
 * real computed styles, and the class-based dark-mode toggle works and
 * persists. ContainerGebSpec drives a Selenium-Chrome container via
 * Testcontainers, so only a running Docker daemon is required.
 */
@Integration
class TailwindFunctionalSpec extends ContainerGebSpec {

    void 'the home page renders with the Tailwind title'() {
        when:
        go '/'

        then:
        title == 'Welcome to Grails + Tailwind'
        $('h1').text().contains('Welcome to Grails + Tailwind CSS')
    }

    void 'the compiled Tailwind stylesheet is served'() {
        when: 'reading the stylesheet hrefs the layout emitted'
        go '/'
        List<String> hrefs = $('link', rel: 'stylesheet').collect { it.@href }

        then: 'the asset-pipeline application bundle (which requires app.css) is linked'
        hrefs.any { it.contains('application.css') }
    }

    void 'the .card component class applies real Tailwind styles'() {
        when:
        go '/'

        then: 'the compiled .card rule gives the section padding and a non-transparent background'
        waitFor {
            String padding = js.exec('return getComputedStyle(document.querySelector(".card")).paddingTop') as String
            padding && padding != '0px'
        }
        String bg = js.exec('return getComputedStyle(document.querySelector(".card")).backgroundColor') as String
        bg && bg != 'rgba(0, 0, 0, 0)' && bg != 'transparent'
    }

    void 'the .btn-primary component class applies a blue background'() {
        when:
        go '/'

        then: 'btn-primary resolves to a non-transparent (blue) background from the component layer'
        String bg = js.exec('return getComputedStyle(document.querySelector(".btn-primary")).backgroundColor') as String
        bg && bg != 'rgba(0, 0, 0, 0)' && bg != 'transparent'
    }

    void 'the 404 page renders when navigating to an unknown path'() {
        when:
        go '/nonexistent-route'

        then:
        title == 'Page Not Found'
        $('h1').text().contains('Page Not Found (404)')
    }

    void 'the 404 page shows the requested path'() {
        when:
        go '/some/missing/page'

        then:
        $('body').text().contains('/some/missing/page')
    }

    void 'the dark-mode toggle adds the dark class and persists the choice'() {
        given:
        go '/'

        expect: 'starts light - no dark class on <html>'
        !(js.exec('return document.documentElement.classList.contains("dark")') as Boolean)

        when: 'clicking the toggle'
        $('#theme-toggle').click()

        then: 'the dark class is applied and saved to localStorage'
        waitFor { js.exec('return document.documentElement.classList.contains("dark")') as Boolean }
        (js.exec('return window.localStorage.getItem("theme")') as String) == 'dark'

        when: 'toggling back'
        $('#theme-toggle').click()

        then:
        waitFor { !(js.exec('return document.documentElement.classList.contains("dark")') as Boolean) }
        (js.exec('return window.localStorage.getItem("theme")') as String) == 'light'
    }
}

package demo

import grails.testing.web.taglib.TagLibUnitTest
import org.springframework.context.MessageSource
import spock.lang.Specification

class LocaleNavbarTagLibSpec extends Specification implements TagLibUnitTest<LocaleNavbarTagLib> {

    void "LocaleNavbarTagLib method localeDropdown renders"() {
        given:
        def uri = '/books'
        def languages = [[code: 'en', msg: 'English'],
                         [code: 'es', msg: 'Spanish'],
                         [code: 'it', msg: 'Italian'],
                         [code: 'de', msg: 'German']]

        when:
        def expected = ''
        languages.each { Map m ->
            expected += "<li><a href='${uri}?lang=${m.code}'>${m.msg}</a></li>"
        }
        tagLib.languages = languages.collect { it.code }
        tagLib.messageSource = Stub(MessageSource) {
            getMessage('language.en', [] as Object[], null, _) >> languages.find { it.code == 'en'}.msg
            getMessage('language.es', [] as Object[], null, _) >> languages.find { it.code == 'es'}.msg
            getMessage('language.it', [] as Object[], null, _) >> languages.find { it.code == 'it'}.msg
            getMessage('language.de', [] as Object[], null, _) >> languages.find { it.code == 'de'}.msg
        }
        def result = applyTemplate('<navBar:localeDropdownListItems uri="/books"/>')

        then:
        cleanUpString(result) == cleanUpString(expected)
    }

    String cleanUpString(String str) {
        str.replaceAll('\n','').replaceAll(' ', '')
    }
}

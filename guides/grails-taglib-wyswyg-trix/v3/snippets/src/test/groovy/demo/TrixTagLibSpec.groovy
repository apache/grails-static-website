package demo

import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@SuppressWarnings(['LineLength', 'MethodName'])
class TrixTagLibSpec extends Specification implements TagLibUnitTest<TrixTagLib> {

    void 'test trix editor markup is created'() {
        when:
        def expected = '''<input id="x" type="hidden" name="content" value="Editor content goes here" /><trix-editor input="x"></trix-editor>'''
        def output = applyTemplate('<trix:editor name="content" id="x" value="Editor content goes here"/>') // <1>

        then:
        output == expected
    }

    void 'trix editor value and id parameters are optional'() {
        given:
        def expected = '<input id="messsage" type="hidden" name="messsage" /><trix-editor input="messsage"></trix-editor>'

        expect:
        tagLib.editor(name: 'messsage') == expected // <2>
    }
}

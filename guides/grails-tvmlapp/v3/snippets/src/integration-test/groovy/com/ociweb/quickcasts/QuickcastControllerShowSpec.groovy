package com.ociweb.quickcasts

import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import groovy.xml.XmlUtil
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Specification
import org.springframework.beans.factory.annotation.Value

@Rollback
@Integration
class QuickcastControllerShowSpec extends Specification {

    def "test product template is rendered correctly"() {
        given:
        def expected = '''<document>
    <productTemplate>
        <banner>
            <infoList>
                <info>
                    <header>
                        <title>Authors</title>
                    </header>
                    <text>Jeff Scott Brown</text>
                </info>
            </infoList>
            <stack>
                <title>Interceptors - Grails 3</title>
                <subtitle>#1 - Grails Quickcast from OCI</subtitle>
                <row>
                    <text>17min 1sec</text>
                    <text>2016</text>
                </row>
                <description allowsZooming="true" moreLabel="more">This Quickcast assumes only basic familiarity with Groovy (which is pretty darn expressive anyway) and the MVC concept (which you already know). Also serves as an excellent introduction to the interceptor pattern in any language, because Grails' behind-the-scenes legwork lets you focus on the logic of the pattern.</description>
                <row>
                    <buttonLockup onselect="playMedia('http://localhost:8888/grails_quickcast_1_interceptors.mp4', 'video')">
                        <badge src="resource://button-play"/>
                        <title>Play</title>
                    </buttonLockup>
                </row>
            </stack>
            <heroImg src="http://localhost:8888/quickcast_interceptor.png"/>
        </banner>
        <shelf>
            <header>
                <title>Viewers also Watched</title>
            </header>
            <section>
                <lockup onselect="getDocument('quickcast/1')">
                    <img src="http://localhost:8888/quickcast_jsonviews.png" width="150" height="226"/>
                    <title>JSON Views - Grails 3</title>
                </lockup>
                <lockup onselect="getDocument('quickcast/1')">
                    <img src="http://localhost:8888/quickcast_multiprojectbuilds.png" width="150" height="226"/>
                    <title>Multi-Project Builds - Grails 3</title>
                </lockup>
                <lockup onselect="getDocument('quickcast/1')">
                    <img src="http://localhost:8888/quickcast_angularscaffolding.png" width="150" height="226"/>
                    <title>Angular Scaffolding - Grails 3</title>
                </lockup>
            </section>
        </shelf>
    </productTemplate>
</document>
'''
        when:
        RestBuilder rest = new RestBuilder()
        def resp = rest.get("http://localhost:${serverPort}/quickcast/1")

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setNormalizeWhitespace(true)

        then:
        resp.status == 200
        XMLUnit.compareXML(XmlUtil.serialize(resp.xml), expected).identical()
    }
}

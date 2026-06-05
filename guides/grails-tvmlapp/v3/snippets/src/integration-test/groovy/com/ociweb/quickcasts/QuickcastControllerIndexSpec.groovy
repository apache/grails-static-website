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
class QuickcastControllerIndexSpec extends Specification {

    def "test stack template is rendered correctly"() {

        given:
        def expected = '''<document>
    <stackTemplate>
        <banner>
            <title>Grails Quickcasts</title>
        </banner>
        <collectionList>
            <shelf>
                <section>
                    <lockup onselect="getDocument('quickcast/1')">
                        <img src="http://localhost:8888/quickcast_interceptor.png" width="150" height="226"/>
                        <title>Interceptors - Grails 3</title>
                    </lockup>
                    <lockup onselect="getDocument('quickcast/2')">
                        <img src="http://localhost:8888/quickcast_jsonviews.png" width="150" height="226"/>
                        <title>JSON Views - Grails 3</title>
                    </lockup>
                    <lockup onselect="getDocument('quickcast/3')">
                        <img src="http://localhost:8888/quickcast_multiprojectbuilds.png" width="150" height="226"/>
                        <title>Multi-Project Builds - Grails 3</title>
                    </lockup>
                    <lockup onselect="getDocument('quickcast/4')">
                        <img src="http://localhost:8888/quickcast_angularscaffolding.png" width="150" height="226"/>
                        <title>Angular Scaffolding - Grails 3</title>
                    </lockup>
                    <lockup onselect="getDocument('quickcast/5')">
                        <img src="http://localhost:8888/quickcast_retrievingruntimeconfigvalues.png" width="150" height="226"/>
                        <title>Retrieving Runtime Config Values - Grails 3</title>
                    </lockup>
                    <lockup onselect="getDocument('quickcast/6')">
                        <img src="http://localhost:8888/quickcast_developinggrailsappswithintellij.png" width="150" height="226"/>
                        <title>Developing Grails Application with IntelliJ IDEA - Grails 3</title>
                    </lockup>
                </section>
            </shelf>
        </collectionList>
    </stackTemplate>
</document>
'''
        when:
        RestBuilder rest = new RestBuilder()
        def resp = rest.get("http://localhost:${serverPort}/quickcast")

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setNormalizeWhitespace(true)

        then:
        resp.status == 200
        XMLUnit.compareXML(XmlUtil.serialize(resp.xml), expected).identical()
    }
}

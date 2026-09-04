/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.gradle.tasks

import spock.lang.Specification

class RenderSiteTaskSpec extends Specification {

    def 'menu highlighting matches paths independently of the configured site origin'() {
        given: 'a menu item with a full URL and a path to match'
            def menu = "<li><a href='https://grails.apache.org/faq.html'>FAQ</a></li>"

        expect: 'the menu item is highlighted when the path matches, regardless of the site origin'
            RenderSiteTask.highlightMenu(menu, 'faq.html') ==
                    "<li class='active'><a href='https://grails.apache.org/faq.html'>FAQ</a></li>"
    }

    def 'menu highlighting uses the Guides path for the Guides landing page'() {
        given: 'the shared menu and the Guides landing page path'
            def menu = """
                <li><a href='https://grails.apache.org/blog/index.html'>Blog</a></li>
                <li><a href='https://grails.apache.org/guides'>Guides</a></li>
            """.stripIndent().trim()

        expect: 'the Guides link is highlighted instead of the Blog archive link'
            RenderSiteTask.highlightMenu(menu, '/guides') == """
                <li><a href='https://grails.apache.org/blog/index.html'>Blog</a></li>
                <li class='active'><a href='https://grails.apache.org/guides'>Guides</a></li>
            """.stripIndent().trim()
    }

    def 'shared partial placeholders resolve for Guide rendering'() {
        when: 'a shared partial is resolved with placeholders for the site URL and Kapa widget script'
            def partial = RenderSiteTask.resolvePartial(
                    "<link href='[%url]/stylesheets/screen.css'>[%kapa]",
                    'http://127.0.0.1:8080')

        then: 'the placeholders are replaced with the actual values'
            partial.contains('http://127.0.0.1:8080/stylesheets/screen.css')
            partial.contains('https://widget.kapa.ai/kapa-widget.bundle.js')
            !partial.contains('[%url]')
            !partial.contains('[%kapa]')
    }

    def 'Guide layout loads the public Algolia assets'() {
        given: 'the layout.html file from the guides resources'
            def rootDir = new File(System.getProperty('user.dir')).name == 'buildSrc' ?
                    new File(System.getProperty('user.dir')).parentFile :
                    new File(System.getProperty('user.dir'))
            def layout = new File(rootDir, 'guides/resources/style/layout.html').getText('UTF-8')

        expect: 'the layout includes the Algolia configuration and assets'
            with(layout) {
                contains('algolia-config.js')
                contains('algolia-search.css')
                contains('algolia-search.js')
                contains("'\${siteUrl}/stylesheets/algolia-search.css'")
                contains("'\${siteUrl}/javascripts/algolia-search.js'")
            }
    }
}

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
package website.model.guides

import spock.lang.Specification

class GuidesPageSpec extends Specification {

    def 'orderedVersionKeys returns majors newest first'() {
        given:
        def guide = new GrailsVersionedGuide(
                name: 'multi',
                title: 'Multi',
                category: 'Advanced Grails',
                grailsMayorVersionTags: [
                        3: ['old'],
                        4: ['mid'],
                        6: ['recent'],
                        8: ['modern'],
                ]
        )

        expect:
        GuidesPage.orderedVersionKeys(guide) == [8, 6, 4, 3]
    }

    def 'renderGuide emits multi-version chips newest first'() {
        given:
        def guide = new GrailsVersionedGuide(
                name: 'multi',
                title: 'Multi Guide',
                category: 'Advanced Grails',
                grailsMayorVersionTags: [
                        3: ['a'],
                        8: ['b'],
                        6: ['c'],
                ]
        )

        when:
        String html = GuidesPage.renderGuide(guide)

        then:
        html.indexOf('grails8') < html.indexOf('grails6')
        html.indexOf('grails6') < html.indexOf('grails3')
        html.contains('/multi/8/guide/index.html')
        html.contains('/multi/6/guide/index.html')
        html.contains('/multi/3/guide/index.html')
    }

    def 'renderGuide with version filter collapses multi-version row to one link'() {
        given:
        def guide = new GrailsVersionedGuide(
                name: 'multi',
                title: 'Multi Guide',
                category: 'Advanced Grails',
                grailsMayorVersionTags: [
                        4: ['legacy'],
                        8: ['modern'],
                ]
        )

        when:
        String html = GuidesPage.renderGuide(guide, '8')

        then:
        html.contains('/multi/8/guide/index.html')
        html.contains('Multi Guide')
        !html.contains('grails4')
        !html.contains('/multi/4/guide/index.html')
    }

    def 'guidesForVersionList returns every matching guide uncapped newest first'() {
        given:
        List<Guide> guides = (1..12).collect { int i ->
            new SingleGuide(
                    name: "guide-$i",
                    title: "Guide $i",
                    category: 'Web Layer',
                    versionNumber: '8',
                    publicationDate: Date.parse('yyyy-MM-dd', "2026-05-${String.format('%02d', i)}"),
                    tags: ['t'],
                    authors: ['A'],
            )
        }
        guides << new SingleGuide(
                name: 'old',
                title: 'Old Guide',
                category: 'Web Layer',
                versionNumber: '4',
                publicationDate: Date.parse('yyyy-MM-dd', '2017-01-01'),
                tags: ['t'],
                authors: ['A'],
        )

        when:
        List<Guide> v8 = GuidesPage.guidesForVersionList('8', guides)

        then:
        v8.size() == 12
        v8*.name == (12..1).collect { "guide-$it" }
        v8.every { it.name != 'old' }
    }

    def 'guidesForVersion HTML includes every guide and a count'() {
        given:
        List<Guide> guides = [
                new SingleGuide(
                        name: 'a',
                        title: 'Alpha',
                        category: 'GORM',
                        versionNumber: '8',
                        publicationDate: Date.parse('yyyy-MM-dd', '2026-06-01'),
                        tags: [],
                        authors: [],
                ),
                new SingleGuide(
                        name: 'b',
                        title: 'Beta',
                        category: 'Web Layer',
                        versionNumber: '8',
                        publicationDate: Date.parse('yyyy-MM-dd', '2026-05-01'),
                        tags: [],
                        authors: [],
                ),
        ]

        when:
        String html = GuidesPage.guidesForVersion('8', guides)

        then:
        html.contains('2 guides for Grails 8')
        html.contains('/a/8/guide/index.html')
        html.contains('/b/8/guide/index.html')
        html.contains('Alpha')
        html.contains('Beta')
    }

    def 'sortGuidesForDisplay puts preferred version first'() {
        given:
        def v8 = new SingleGuide(
                name: 'modern',
                title: 'Modern',
                category: 'Web Layer',
                versionNumber: '8',
                publicationDate: Date.parse('yyyy-MM-dd', '2026-05-01'),
                tags: [],
                authors: [],
        )
        def v4 = new SingleGuide(
                name: 'legacy',
                title: 'Legacy',
                category: 'Web Layer',
                versionNumber: '4',
                publicationDate: Date.parse('yyyy-MM-dd', '2018-01-01'),
                tags: [],
                authors: [],
        )
        def multi = new GrailsVersionedGuide(
                name: 'multi',
                title: 'Multi',
                category: 'Web Layer',
                publicationDate: Date.parse('yyyy-MM-dd', '2020-01-01'),
                grailsMayorVersionTags: [4: ['x'], 6: ['y']],
        )

        when:
        List<Guide> sorted = GuidesPage.sortGuidesForDisplay([v4, multi, v8], '8')

        then: 'preferred major first; remaining guides keep newest-first date order'
        sorted*.name == ['modern', 'multi', 'legacy']
    }

    def 'index mainContent features every Grails 8 guide and version page lists them all'() {
        given:
        List<Guide> guides = [
                new SingleGuide(
                        name: 'g8-a',
                        title: 'G8 A',
                        category: 'Web Layer',
                        versionNumber: '8',
                        publicationDate: Date.parse('yyyy-MM-dd', '2026-06-01'),
                        tags: ['htmx'],
                        authors: ['A'],
                ),
                new SingleGuide(
                        name: 'g8-b',
                        title: 'G8 B',
                        category: 'GORM',
                        versionNumber: '8',
                        publicationDate: Date.parse('yyyy-MM-dd', '2026-05-15'),
                        tags: ['gorm'],
                        authors: ['A'],
                ),
                new SingleGuide(
                        name: 'legacy',
                        title: 'Legacy Guide',
                        category: 'Web Layer',
                        versionNumber: '4',
                        publicationDate: Date.parse('yyyy-MM-dd', '2017-01-01'),
                        tags: ['old'],
                        authors: ['A'],
                ),
        ]
        Set tags = [] as Set

        when:
        String index = GuidesPage.mainContent(guides, tags)
        String versionPage = GuidesPage.mainContent(guides, tags, null, null, '8')

        then: 'index leads with the full Grails 8 catalogue'
        index.contains('Grails 8 Guides')
        index.contains('/g8-a/8/guide/index.html')
        index.contains('/g8-b/8/guide/index.html')
        index.contains('Legacy Guide')

        and: 'version page lists every G8 guide uncapped with a count'
        versionPage.contains('2 guides for Grails 8')
        versionPage.contains('/g8-a/8/guide/index.html')
        versionPage.contains('/g8-b/8/guide/index.html')
        !versionPage.contains('/legacy/4/guide/index.html')
        versionPage.contains('G8 A')
        versionPage.contains('G8 B')
    }

    def 'real conf/guides.yml exposes every version-8 entry on the version page'() {
        given:
        File yml = locateGuidesYml()

        expect:
        yml != null

        when:
        List<Guide> guides = GuidesFetcher.fetchGuides(yml, false)
        List<String> v8Names = GuidesPage.guidesForVersionList('8', guides)*.name
        String html = GuidesPage.guidesForVersion('8', guides)
        String grid = GuidesPage.categoryGrid(guides, '8', true)

        then:
        v8Names.size() >= 13
        v8Names.every { String name -> html.contains("/${name}/8/guide/index.html") }
        html.contains("${v8Names.size()} guides for Grails 8")
        // Categorized companion must not silently drop a modern guide whose
        // category was missing from the hardcoded grid (regression: Grails REST APIs).
        v8Names.every { String name -> grid.contains("/${name}/8/guide/index.html") }
        grid.contains('Grails REST APIs')
    }

    private static File locateGuidesYml() {
        [
                new File('conf/guides.yml'),
                new File('../conf/guides.yml'),
                new File('../../conf/guides.yml'),
        ].find { it.exists() }
    }
}

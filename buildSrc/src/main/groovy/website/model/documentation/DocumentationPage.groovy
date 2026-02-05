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
package website.model.documentation

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.yaml.snakeyaml.Yaml

import static website.utils.RenderUtils.renderHtml

@CompileStatic
class DocumentationPage {

    /**
     * Renders a single documentation category as an HTML guide group.
     * Displays the category's icon, title, and a list of linked items.
     *
     * @param category the documentation category to render
     * @return HTML string representing the category section
     */
    @CompileDynamic
    private static String renderCategory(DocumentationCategory category) {
        renderHtml {
            div(class: 'guide-group') {
                div(class: 'guide-group-header') {
                    img(src: category.image, alt: category.title)
                    h2(category.title)
                }
                ul {
                    category.items.each { item ->
                        li {
                            a(href: item.url, item.title)
                        }
                    }
                }
            }
        }
    }

    /**
     * Renders the documentation links section for a specific Grails version.
     * Includes links to the User Guide and API Reference.
     *
     * @param version the Grails version string (e.g., "6.2.0", "snapshot")
     * @return HTML string with documentation links for the specified version
     */
    @CompileDynamic
    static String renderDocumentation(String version) {
        renderHtml {
            div(class: 'guide-group') {
                if (version) {
                    div(class: 'guide-group-header') {
                        img(
                                src: '[%url]/images/documentation.svg',
                                alt: "Grails Version ($version)"
                        )
                        h2(
                                DocumentationPage.resolveDocumentationName(version)
                        )
                    }
                    ul {
                        li {
                            a(
                                    href: "https://grails.apache.org/docs/$version/",
                                    'User Guide'
                            )
                        }
                        li {
                            a(
                                    href: "https://grails.apache.org/docs/$version/api/",
                                    'API Reference'
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves a human-readable documentation title based on the version string.
     * Determines whether the version is a Snapshot, Milestone, Release Candidate, or Latest release.
     *
     * @param version the Grails version string
     * @return formatted documentation title (e.g., "Latest Version (6.2.0) Documentation")
     */
    static String resolveDocumentationName(String version) {
        def v = version.toLowerCase()
        def label =
                v.endsWith('-snapshot') || v.contains('snapshot') ? 'Snapshot' :
                        version.contains('.M')                    ? 'Milestone' :
                        version.contains('.RC')                   ? 'Release Candidate' :
                                                                    'Latest'

        "$label Version ($version) Documentation"
    }

    /**
     * Generates the main documentation page HTML content.
     * Displays pre-release, snapshot, and latest version documentation links,
     * along with categorized documentation for GORM, Security, and other modules.
     * Also includes version selectors for browsing older documentation.
     *
     * @param releases the YAML file containing Grails release information
     * @param modules the YAML file containing module/category definitions
     * @return complete HTML string for the documentation page
     */
    @CompileDynamic
    static String mainContent(File releases, File modules) {
        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    h1('Documentation')
                }
            }
            def preRelease = SiteMap.latestPreReleaseVersion(releases)
            def latest = SiteMap.latestVersion(releases)
            def categories = DocumentationPage.categories(modules)
            div(class: 'content') {
                div(class: 'two-columns') {
                    div(class: 'odd column'){
                        if (preRelease > latest) {
                            mkp.yieldUnescaped(
                                    DocumentationPage.renderDocumentation(preRelease.versionText)
                            )
                        }
                        mkp.yieldUnescaped(
                                DocumentationPage.renderDocumentation('snapshot')
                        )
                        mkp.yieldUnescaped(
                                DocumentationPage.renderCategory(categories.find {
                                    it.title == 'GORM - Data Access Toolkit'
                                })
                        )
                        mkp.yieldUnescaped(
                                DocumentationPage.renderCategory(categories.find {
                                    it.title == 'Security'
                                })
                        )

                    }
                    div(class: 'column') {
                        mkp.yieldUnescaped(
                                DocumentationPage.renderDocumentation(latest.versionText)
                        )
                        div(class: 'older-versions') {
                            h3(
                                    class: 'column-header',
                                    style: 'margin-bottom: 10px',
                                    'Older Version'
                            )
                            p('Browse previous versions\' documentation since Grails 1.2.0')
                            div(class: 'version-selector') {
                                h4('Single Page - User Guide')
                                select(onchange: "window.location.href='https://grails.apache.org/docs/' + this.value + '/guide/single.html'") {
                                    option('Select a version')
                                    mkp.yield('[%versions]')
                                }
                            }
                            div(class: 'version-selector') {
                                h4('User Guide')
                                select(onchange: "window.location.href='https://grails.apache.org/docs/' + this.value") {
                                    option('Select a version')
                                    mkp.yield('[%versions]')
                                }
                            }
                            div(class: 'version-selector') {
                                h4('API Reference')
                                select(onchange: "window.location.href='https://grails.apache.org/docs/' + this.value + '/api'") {
                                    option('Select a version')
                                    mkp.yield('[%versions]')
                                }
                            }
                        }
                        ['Upgrade', 'Testing', 'Views', 'Async', 'Database', 'Redis'].each { title ->
                            mkp.yieldUnescaped(
                                    DocumentationPage.renderCategory(categories.find {
                                        it.title == title
                                    })
                            )
                        }
                    }

                }
            }
        }
    }

    /**
     * Parses the modules YAML file and groups documentation items by category.
     * Each category contains metadata (title, image, description, URL) and a list of items.
     *
     * @param modules the YAML file containing module definitions
     * @return collection of {@link DocumentationCategory} objects with their items populated
     */
    @CompileDynamic
    private static Collection<DocumentationCategory> categories(File modules) {
        def model = modules.newInputStream().withCloseable { is ->
            new Yaml().load(is) as Map
        }
        Map<String, DocumentationCategory> byCategory = [:].withDefault { key ->
            new DocumentationCategory(title: key)
        }
        (model.modules as Map).each { k, v ->
            def cat = byCategory[v['category']]
            cat.with {
                image = v['categoryImage'] ?: null
                description = v['categoryDescription'] ?: null
                url = v['categoryUrl'] ?: null
            }
            cat.items << new DocumentationItem(url: v['url'], title: v['title'])
        }
        byCategory.values()
    }

    /**
     * Represents a documentation category containing related documentation items.
     * Categories group items like "GORM", "Security", "Testing", etc.
     */
    @CompileStatic
    static class DocumentationCategory {
        String description
        String image
        String title
        String url
        List<DocumentationItem> items = []
    }

    /**
     * Represents a single documentation item within a category.
     * Contains a title and URL linking to the documentation resource.
     */
    @CompileStatic
    static class DocumentationItem {
        String title
        String url
    }
}

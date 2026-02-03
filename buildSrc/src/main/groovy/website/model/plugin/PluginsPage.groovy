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
package website.model.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import website.model.guides.TagUtils
import website.model.tags.TagCloud
import website.utils.DateUtils

import static website.utils.RenderUtils.renderHtml

@CompileStatic
class PluginsPage {

    /**
     * Formats a plugin name for display:
     * - Replaces hyphens with spaces
     * - Capitalizes the first letter of each word
     *
     * Example: "grails-csrf" -> "Grails Csrf"
     */
    static String formatPluginName(String name) {
        if (!name) return name
        name.split('-')
            .collect { word -> word ? word.capitalize() : '' }
            .join(' ')
    }

    @CompileDynamic
    static String mainContent(
            String siteUrl,
            List<Plugin> plugins,
            String title,
            List<Plugin> filteredPlugins
    ) {
        def sortedByName = (plugins ?: [])
                .toSorted { a, b -> a.name?.toLowerCase() <=> b.name?.toLowerCase() }
        def sortedByDate = (plugins ?: [])
                .toSorted { a, b -> b.updated <=> a.updated }.take(5)
        def sortedByStars = (plugins ?: [])
                .findAll { it.githubStars != null && it.githubStars > 0 }
                .toSorted { a, b -> b.githubStars <=> a.githubStars }.take(5)

        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    h1('Grails Plugins')
                }
            }
            div(class: 'content plugins-page') {
                if (title !== 'Grails Plugins') {
                    div(class: 'breadcrumbs') {
                        a(href: siteUrl + '/plugins.html', 'All Grails Plugins')
                        span(' » ')
                        span(title)
                    }
                    // Show filtered plugins directly
                    div(class: 'plugins-section') {
                        mkp.yieldUnescaped(renderPluginGrid(filteredPlugins ?: plugins, title))
                    }
                } else {
                    // Tab navigation
                    nav(class: 'plugins-nav') {
                        a(href: '#all', class: 'nav-tab active', 'data-tab': 'all', "All (${sortedByName.size()})")
                        a(href: '#recent', class: 'nav-tab', 'data-tab': 'recent', 'Recently Updated')
                        a(href: '#stars', class: 'nav-tab', 'data-tab': 'stars', 'Most Stars')
                        a(href: '#tags', class: 'nav-tab', 'data-tab': 'tags', 'Tags')
                        a(href: '#owners', class: 'nav-tab', 'data-tab': 'owners', 'Owners')
                        a(href: '#links', class: 'nav-tab', 'data-tab': 'links', 'Links')
                    }

                    // ALL tab content
                    div(class: 'tab-content active', id: 'all') {
                        div(class: 'search-pagination-row') {
                            div(class: 'search-box-inline') {
                                input(type: 'text', id: 'query', placeholder: 'Search plugins...')
                                button(type: 'button', class: 'search-clear-btn', title: 'Clear search', '×')
                            }
                            div(class: 'pagination-container top', '')
                        }
                        div(class: 'plugins all-plugins') {
                            ul(class: 'plugin-list') {
                                sortedByName.each { plugin ->
                                    mkp.yieldUnescaped(PluginsPage.renderSinglePlugin(plugin))
                                }
                            }
                        }
                        div(class: 'guide-group no-results hidden') {
                            div(class: 'guide-group-header') {
                                h2('No results found!')
                            }
                        }
                        h3(class: 'search-results-label hidden', 'Plugins Filtered by: ') {
                            span(class: 'query-label', '')
                        }
                        div(class: 'search-results hidden', '')
                        div(class: 'search-pagination-row bottom-only') {
                            div(class: 'pagination-container bottom', '')
                        }
                    }

                    // RECENTLY UPDATED tab content
                    div(class: 'tab-content', id: 'recent') {
                        div(class: 'plugins') {
                            ul(class: 'plugin-list') {
                                sortedByDate.each { plugin ->
                                    mkp.yieldUnescaped(PluginsPage.renderSinglePlugin(plugin))
                                }
                            }
                        }
                    }

                    // MOST STARS tab content
                    div(class: 'tab-content', id: 'stars') {
                        div(class: 'plugins') {
                            ul(class: 'plugin-list') {
                                sortedByStars.each { plugin ->
                                    mkp.yieldUnescaped(PluginsPage.renderSinglePlugin(plugin))
                                }
                                mkp.yieldUnescaped('')
                            }
                        }
                    }

                    // TAGS tab content
                    div(class: 'tab-content', id: 'tags') {
                        def tags = TagUtils.populateTagsByPlugins(plugins)
                        mkp.yieldUnescaped(TagCloud.tagCloud("$siteUrl/plugins/tags", tags, false))
                    }

                    // OWNERS tab content
                    div(class: 'tab-content', id: 'owners') {
                        def ownerTags = TagUtils.populateTagsByPluginOwners(plugins)
                        mkp.yieldUnescaped(TagCloud.tagCloud("$siteUrl/plugins/owners", ownerTags, false))
                    }

                    // LINKS tab content
                    div(class: 'tab-content', id: 'links') {
                        mkp.yieldUnescaped(PluginsPage.linksContent())
                    }
                }
            }
        }
    }

    @CompileDynamic
    static String renderPluginGrid(List<Plugin> plugins, String title) {
        def sortedPlugins = (plugins ?: []).toSorted { a, b -> a.name?.toLowerCase() <=> b.name?.toLowerCase() }
        renderHtml {
            h2(class: 'section-title', title)
            ul(class: 'plugin-list') {
                sortedPlugins.each { plugin ->
                    mkp.yieldUnescaped(PluginsPage.renderSinglePlugin(plugin))
                }
            }
        }
    }

    @CompileDynamic
    static String linksContent() {
        List<Map<String, String>> links = [
                [url: 'https://grails.apache.org/blog/2021-04-07-publish-grails-plugin-to-maven-central.html', title: 'Publishing Guide', desc: 'Learn how to publish your Grails plugin to Maven Central'],
                [url: 'https://github.com/apache/grails-plugins-metadata', title: 'Plugin Portal on GitHub', desc: 'Contribute to the Grails plugin metadata repository'],
        ]
        renderHtml {
            div(class: 'links-section') {
                h2(class: 'section-title', 'Useful Links')
                ul(class: 'links-list') {
                    links.each { link ->
                        li {
                            a(href: link.url, target: '_blank', link.title)
                            if (link.desc) {
                                p(link.desc)
                            }
                        }
                    }
                }
            }
        }
    }

    @CompileDynamic
    static String renderSinglePlugin(Plugin plugin) {
        def displayName = formatPluginName(plugin.name)
        def cardClass = plugin.deprecated ? 'plugin deprecated' : 'plugin'
        renderHtml {
            li(class: cardClass) {
                // Header row: Plugin name + version dropdown
                div(class: 'plugin-header') {
                    h3(class: 'name') {
                        if (plugin.vcsUrl) {
                            a(href: plugin.vcsUrl, target: '_blank', displayName)
                        } else {
                            mkp.yield(displayName)
                        }
                    }
                    // Version dropdown pill
                    if (plugin.versions && plugin.versions.size() > 0) {
                        div(class: 'version-dropdown') {
                            // Current version display (clickable to toggle dropdown)
                            div(class: 'version-current') {
                                div(class: 'version-row') {
                                    span(class: 'version', plugin.latestVersion)
                                    if (plugin.grailsVersion) {
                                        span(class: 'grails-compat', plugin.grailsVersion)
                                    }
                                    if (plugin.versions.size() > 1) {
                                        span(class: 'dropdown-arrow', '▾')
                                    }
                                }
                                if (plugin.updated) {
                                    div(class: 'version-date-row', DateUtils.format_MMM_D_YYYY(plugin.updated))
                                }
                            }
                            // Dropdown content with all versions
                            if (plugin.versions.size() > 1) {
                                div(class: 'version-list') {
                                    plugin.versions.each { v ->
                                        div(class: 'version-item') {
                                            span(class: 'ver', v.version)
                                            if (v.grailsVersion) {
                                                span(class: 'compat', v.grailsVersion)
                                            }
                                            if (v.date) {
                                                span(class: 'date', DateUtils.format_MMM_D_YYYY(v.date))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (plugin.latestVersion || plugin.grailsVersion) {
                        // Fallback for plugins without versions array
                        div(class: 'plugin-meta') {
                            div(class: 'version-row') {
                                if (plugin.latestVersion) {
                                    span(class: 'version', plugin.latestVersion)
                                }
                                if (plugin.grailsVersion) {
                                    span(class: 'grails-compat', title: 'Grails version compatibility', plugin.grailsVersion)
                                }
                            }
                            if (plugin.updated) {
                                div(class: 'version-date-row', DateUtils.format_MMM_D_YYYY(plugin.updated))
                            }
                        }
                    }
                }

                // Description
                if (plugin.desc) {
                    p(class: 'desc', title: plugin.desc) { mkp.yield(plugin.desc) }
                }

                // Maven coordinates with version (for copy/paste into Gradle)
                if (plugin.coords && plugin.latestVersion) {
                    def fullCoords = "$plugin.coords:$plugin.latestVersion"
                    // Add word break opportunities after colons for better line breaking
                    def displayCoords = fullCoords.toString().replace(':', ':<wbr>')
                    div(class: 'coords-wrapper') {
                        if (plugin.mavenRepo && plugin.coords) {
                            // Convert groupId:artifactId to path: com.example:my-plugin -> com/example/my-plugin
                            def coordParts = plugin.coords.split(':')
                            def groupPath = coordParts[0].replace('.', '/')
                            def artifactId = coordParts.length > 1 ? coordParts[1] : ''
                            def artifactUrl = "$plugin.mavenRepo/$groupPath/$artifactId/$plugin.latestVersion"
                            a(href: artifactUrl, class: 'maven-link', target: '_blank', title: 'View on Maven repository') {
                                code(class: 'coords') {
                                    mkp.yieldUnescaped(displayCoords)
                                }
                            }
                        } else {
                            code(class: 'coords') {
                                mkp.yieldUnescaped(displayCoords)
                            }
                        }
                        button(class: 'copy-btn', 'data-coords': fullCoords, title: 'Copy to clipboard', '📋')
                    }
                } else if (plugin.coords) {
                    def displayCoords = plugin.coords.replace(':', ':<wbr>')
                    div(class: 'coords-wrapper') {
                        code(class: 'coords') {
                            mkp.yieldUnescaped(displayCoords)
                        }
                        button(class: 'copy-btn', 'data-coords': plugin.coords, title: 'Copy to clipboard', '📋')
                    }
                }

                // Plugin card footer: owner, docs/license, deprecated
                div(class: 'plugin-card-footer') {
                    // Left side: owner pill and docs/license
                    div(class: 'footer-left') {
                        if (plugin.owner) {
                            a(href: "[%url]/plugins/owners/${plugin.owner.name}.html", class: 'owner-pill') {
                                span(class: 'owner-icon', '@')
                                mkp.yield(plugin.owner.name)
                            }
                        }
                        if (plugin.docsUrl) {
                            a(href: plugin.docsUrl, class: 'docs-link', target: '_blank', '📖 Docs')
                        }
                        if (plugin.license) {
                            span(class: 'license', "⚖️ ${plugin.license}")
                        }
                    }
                    // Right side: deprecated badge
                    if (plugin.deprecated) {
                        span(class: 'deprecated-badge', title: plugin.deprecated, 'Deprecated')
                    }
                }

                // Labels/tags row with GitHub stars
                if (plugin.labels || plugin.githubStars) {
                    div(class: 'plugin-tags-row') {
                        if (plugin.labels) {
                            ul(class: 'labels') {
                                plugin.labels.each { label ->
                                    li(class: 'label') {
                                        a(href: "[%url]/plugins/tags/${label}.html", "#${label}")
                                    }
                                }
                            }
                        }
                        // GitHub stars - aligned with tags
                        if (plugin.githubStars) {
                            span(class: 'github-stars', "★ ${plugin.githubStars}")
                        }
                    }
                }
            }
        }
    }
}

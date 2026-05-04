/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.model.guides

import java.text.SimpleDateFormat
import java.util.regex.Pattern

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import website.model.tags.Tag
import website.utils.ReadFileUtils

import static website.utils.RenderUtils.renderHtml

@CompileStatic
class GuidesPage {

    public static final Integer NUMBER_OF_LATEST_GUIDES = 8
    public static final Integer TAG_CLOUD_LIMIT = 50
    public static final String GUIDES_URL = 'https://grails.apache.org/guides'

    /**
     * Tag slugs that are version labels masquerading as topics (e.g. {@code grails3},
     * {@code grails8}). These are filtered out of the tag cloud because they
     * dwarf real topic tags by occurrence count and add no navigational value -
     * the version is already implicit in the guide URL.
     */
    private static final Pattern VERSION_TAG_PATTERN = ~/^grails\d+$/

    /**
     * The category-image map. Categories listed here are rendered both on the
     * guides index page AND as standalone category pages under
     * {@code /guides/categories/<slug>.html}. Categories that exist in
     * {@code conf/guides.yml} but are NOT listed here are reachable only via
     * tags / search / Latest Guides.
     *
     * <p>The set has been pruned to the categories that actually carry traffic
     * in the current Grails 7/8 era. Single-guide legacy categories
     * (Grails + RIA, Grails + Android, Grails + Angular, Grails + AngularJS,
     * Grails + iOS) have been dropped - those guides remain accessible via
     * their tag pages and are still indexed by search engines via direct URLs.</p>
     */
    static Map<String, Category> categories = [
            advanced: new Category(name: 'Advanced Grails', image: 'advancedgrails.svg'),
            apprentice: new Category(name: 'Grails Apprentice', image: 'grailaprrentice.svg'),
            async: new Category(name: 'Grails Async', image: 'async.svg'),
            devops: new Category(name: 'Grails + DevOps', image: 'grailsdevops.svg'),
            googlecloud: new Category(name: 'Grails + Google Cloud', image: 'googlecloud.svg'),
            gorm: new Category(name: 'GORM', image: 'gorm.svg'),
            react: new Category(name: 'Grails + React', image: 'react.svg'),
            testing: new Category(name: 'Grails Testing', image: 'testing.svg'),
            vue: new Category(name: 'Grails + Vue.js', image: 'vue.svg'),
            weblayer: new Category(name: 'Web Layer', image: 'views.svg'),
    ]
    
    
    @CompileDynamic
    static String renderGuide(Guide guide) {
        renderHtml {
            li {
                if (guide instanceof SingleGuide) {
                    a(
                            class: (guide.tags.contains('quick-cast') ? 'quick-cast guide' : 'guide'),
                            href: "$GUIDES_URL/${guide.name}/${guide.versionNumber}/guide/index.html", guide.title
                    )
                    guide.tags.each {
                        span(
                                style: 'display: none',
                                class: 'tag', it
                        )
                    }
                } else if (guide instanceof GrailsVersionedGuide) {
                    def multiGuide = (GrailsVersionedGuide) guide
                    div(class: (guide.tags.contains('quick-cast') ? 'quick-cast multi-guide' : 'multi-guide')) {
                        span(class: 'title', guide.title)
                        for (def grailsVersion :  multiGuide.grailsMayorVersionTags.keySet())  {
                            def tagList = multiGuide.grailsMayorVersionTags[grailsVersion] as Set<String>
                            div(class: 'align-left') {
                                a(
                                        class: 'grails-version',
                                        href: "$GUIDES_URL/${multiGuide.name}/${grailsVersion}/guide/index.html"
                                ) {
                                    mkp.yield("grails$grailsVersion")
                                }
                                tagList.each {
                                    span(
                                            style: 'display: none',
                                            class: 'tag',
                                            it
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @CompileDynamic
    static String mainContent(
            List<Guide> guides,
            Set<Tag> tags,
            Category category = null,
            Tag tag = null
    ) {
        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    if (tag || category) {
                        h1 {
                            a(href: '[%url]/index.html', 'Guides')
                            if (tag) {
                                mkp.yield(" → #$tag.title")
                            } else if(category) {
                                mkp.yield(" → $category.name")
                            }
                        }
                    } else {
                        h1('Guides')
                    }
                }
            }
            div(class: 'content') {
                omitEmptyAttributes = true
                omitNullAttributes = true
                div(class: 'two-columns') {
                    div(class: 'column') {
                        mkp.yieldUnescaped(rightColumn(tag, category, guides))
                    }
                    div(class: 'column') {
                        mkp.yieldUnescaped(leftColumn(tag, category, tags))
                        if (tag) {
                            mkp.yieldUnescaped(guideGroupByTag(tag, guides))
                        } else if (category) {
                            mkp.yieldUnescaped(
                                    guideGroupByCategory(
                                            category,
                                            guides.findAll { it.category == category.name },
                                            false
                                    )
                            )
                        } else {
                            div(class: 'search-results') {
                                mkp.yieldUnescaped('')
                            }
                        }
                    }
                }
                // The two-column grid pairs a "primary" category on the left with
                // a complementary one on the right. Reading order goes top-down by
                // pair, so the first pair (apprentice / advanced) is the most
                // prominent. Categories that no longer earn a section in this grid
                // (Grails + Android, Grails + iOS, Grails + Angular, Grails + AngularJS,
                // Grails + RIA) have been removed entirely; their guides remain
                // reachable via tags, search, and the Latest Guides sidebar.
                div(class: 'two-columns') {
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.apprentice, guides, true, 'margin-top: 0'))
                            mkp.yieldUnescaped(guideGroupByCategory(categories.async, guides, true, 'margin-top: 0'))
                        }
                    }
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.advanced, guides, true, 'margin-top: 0'))
                        }
                    }
                }
                div(class: 'two-columns') {
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.weblayer, guides, true, 'margin-top: 0'))
                        }
                    }
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.devops, guides, true, 'margin-top: 0'))
                        }
                    }
                }
                div(class: 'two-columns') {
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.gorm, guides, true, 'margin-top: 0'))
                        }
                    }
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.testing, guides, true, 'margin-top: 0'))
                        }
                    }
                }
                div(class: 'two-columns') {
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.vue, guides, true, 'margin-top: 0'))
                            mkp.yieldUnescaped(guideGroupByCategory(categories.googlecloud, guides))
                        }
                    }
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.react, guides, true, 'margin-top: 0'))
                        }
                    }
                }
            }
        }
    }

    @CompileDynamic
    static String leftColumn(Tag tag, Category category, Set<Tag> tags) {
        renderHtml {
            div {
                if (!(tag || category)) {
                    mkp.yieldUnescaped(GuidesPage.tagCloud(tags))
                }
            }
        }
    }

    @CompileDynamic
    static String rightColumn(Tag tag, Category category, List<Guide> guides) {
        renderHtml {
            div {
                mkp.yieldUnescaped(searchBox(tag, category))
                if (!(tag || category)) {
                    mkp.yieldUnescaped(GuidesPage.latestGuides(guides))
                }
            }
        }
    }

    @CompileDynamic
    static String latestGuides(List<Guide> guides) {
        renderHtml {
            div(class: 'latest-guides') {
                h3(class: 'column-header', 'Latest Guides')
                ul {
                    guides.findAll { it.publicationDate }
                            .sort { a, b -> b.publicationDate <=> a.publicationDate }
                            .take(NUMBER_OF_LATEST_GUIDES)
                            .each { guide ->
                                li {
                                    b(guide.title)
                                    span {
                                        mkp.yield(new SimpleDateFormat('MMM dd, yyyy').format(guide.publicationDate))
                                        mkp.yield(' - ')
                                        mkp.yield(guide.category)
                                    }
                                    a(href: "$GUIDES_URL/${guide.name}/${guide.versionNumber}/guide/index.html", 'Read More')
                                }
                            }
                }
            }
        }
    }

    /**
     * Renders the tag cloud sidebar. Filters out version-label tags
     * (e.g. {@code grails3}, {@code grails8}) that artificially dominate
     * the cloud by occurrence count without adding navigational value, and
     * caps the visible set to the {@link #TAG_CLOUD_LIMIT} most-used tags
     * before re-sorting alphabetically for display.
     */
    @CompileDynamic
    static String tagCloud(Set<Tag> tags) {
        List<Tag> curated = tags
                .findAll { Tag t -> t.title && !VERSION_TAG_PATTERN.matcher(t.title).matches() }
                .sort { Tag a, Tag b -> b.occurrence <=> a.occurrence ?: a.title <=> b.title }
                .take(TAG_CLOUD_LIMIT)
                .sort { Tag a, Tag b -> a.title <=> b.title }
        renderHtml {
            div(class: 'tags-by-topic') {
                h3(class: 'column-header', 'Guides by Tag')
                ul(class: 'tag-cloud') {
                    curated.each { tag ->
                        li(class: "tag$tag.occurrence") {
                            a(href: "$GUIDES_URL/tags/${tag.slug.toLowerCase()}.html", tag.title)
                        }
                    }
                }
            }
        }
    }

    @CompileDynamic
    static String searchBox(Tag tag, Category category) {
        if (!(tag || category)) {
            renderHtml {
                div(class: 'searchbox', style: 'margin-top: 50px !important') {
                    div(class: 'search', style: 'margin-bottom: 0px !important') {
                        input(type: 'text', id: 'query', placeholder: 'SEARCH')
                    }
                }
            }
        } else {
            ''
        }
    }

    @CompileDynamic
    static String guideGroupByCategory(
            Category category,
            List<Guide> guides,
            boolean linkToCategory = true,
            String cssStyle = ''
    ) {
        renderHtml {
            div(class: 'guide-group', style: cssStyle) {
                div(class: 'guide-group-header') {
                    img(
                            src: "[%url]/images/$category.image" as String,
                            alt: category.name
                    )
                    if (linkToCategory)  {
                        a(href: "$GUIDES_URL/categories/${category.slug}.html") {
                            h2(category.name)
                        }
                    } else {
                        h2(category.name)
                    }
                }
                ul {
                    guides
                            .findAll { it.category == category.name }
                            .each { mkp.yieldUnescaped(GuidesPage.renderGuide(it)) }
                }
            }
        }
    }

    @CompileDynamic
    static String guideGroupByTag(Tag tag, List<Guide> guides) {
        renderHtml {
            div(class: 'guide-group') {
                div(class: 'guide-group-header') {
                    img(src: '[%url]/images/documentation.svg', alt: 'Guides')
                    h2("Guides filtered by #$tag.title")
                }
                ul {
                    guides
                            .findAll { Guide guide -> guide.tags.contains(tag.title) }
                            .each { mkp.yieldUnescaped(GuidesPage.renderGuide(it)) }
                }
            }
        }
    }
}

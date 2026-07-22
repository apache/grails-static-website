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

    /**
     * Cap for the "Latest Guides" sidebar on the index. Sized high enough that
     * every current Grails 8 guide still appears there when they are the newest
     * publications - the old value of 8 silently dropped half of the modern set.
     */
    public static final Integer NUMBER_OF_LATEST_GUIDES = 20

    /**
     * Major version featured at the top of the guides index category grid and
     * used as the default "modern" cut when sorting multi-version guide rows.
     */
    public static final String FEATURED_VERSION = '8'

    public static final String GUIDES_URL = 'https://grails.apache.org/guides'

    /**
     * Tag slugs that are version labels masquerading as topics (e.g. {@code grails3},
     * {@code grails8}). The retag pass dropped these from {@code conf/guides.yml}
     * but the filter is kept defensively so any future YAML drift can't
     * resurrect them in the cloud - the version is already implicit in the
     * guide URL.
     */
    private static final Pattern VERSION_TAG_PATTERN = ~/^grails\d+$/

    /**
     * The category-image map. Categories listed here are rendered both on the
     * guides index page AND as standalone category pages under
     * {@code /guides/categories/<slug>.html}. Categories that exist in
     * {@code conf/guides.yml} but are NOT listed here are reachable only via
     * tags / search / Latest Guides.
     *
     * <p>Tags are the primary browse mechanism (see {@link #tagCloud}); the
     * category grid is a small curated set of high-level "tracks" that lets
     * users skim by axis. The set has been pruned to the tracks that actually
     * carry traffic in the current Grails 7/8 era:
     * <ul>
     *   <li>The Web Layer track was added (htmx, tailwind, vite, fields, REST).</li>
     *   <li>The Security track was added - previously these guides were buried
     *       under "Advanced Grails" alongside multi-tenancy and SOAP, even
     *       though spring-security-* alone covers ~10 guides.</li>
     *   <li>Single-version legacy SPA tracks (Angular, AngularJS) and the
     *       per-mobile-OS tracks (iOS, Android) and Grails + RIA were dropped;
     *       their guides remain reachable via tags, search, and direct URL.</li>
     *   <li>"Grails + React" and "Grails + Vue.js" were merged into a single
     *       "Frontend SPA" track since the integration patterns overlap heavily
     *       and neither category alone justified its own column on the index.</li>
     * </ul>
     * </p>
     */
    static Map<String, Category> categories = [
            advanced: new Category(name: 'Advanced Grails', image: 'advancedgrails.svg'),
            apprentice: new Category(name: 'Grails Apprentice', image: 'grailaprrentice.svg'),
            async: new Category(name: 'Grails Async', image: 'async.svg'),
            devops: new Category(name: 'Grails + DevOps', image: 'grailsdevops.svg'),
            cloud: new Category(name: 'Grails + Cloud', image: 'googlecloud.svg'),
            gorm: new Category(name: 'GORM', image: 'gorm.svg'),
            security: new Category(name: 'Security', image: 'security.svg'),
            spa: new Category(name: 'Frontend SPA', image: 'react.svg'),
            testing: new Category(name: 'Grails Testing', image: 'testing.svg'),
            weblayer: new Category(name: 'Web Layer', image: 'views.svg'),
            restapis: new Category(name: 'Grails REST APIs', image: 'restapis.svg'),
    ]


    @CompileDynamic
    static String renderGuide(Guide guide, String versionFilter = null) {
        // Pre-resolve anything MarkupBuilder would otherwise treat as a tag name.
        final boolean multi = guide instanceof GrailsVersionedGuide
        final GrailsVersionedGuide multiGuide = multi ? (GrailsVersionedGuide) guide : null
        final List<Integer> versionKeys = multi ? orderedVersionKeys(multiGuide) : Collections.emptyList()
        final Integer filteredMajor = (multi && versionFilter?.isInteger()) ? versionFilter.toInteger() : null
        final List<String> filteredTags = (multi && filteredMajor != null)
                ? (multiGuide.grailsMayorVersionTags[filteredMajor] ?: []) as List<String>
                : Collections.emptyList() as List<String>

        renderHtml {
            li {
                if (guide instanceof SingleGuide) {
                    String version = versionFilter ?: guide.versionNumber
                    a(
                            class: (guide.tags.contains('quick-cast') ? 'quick-cast guide' : 'guide'),
                            href: "$GUIDES_URL/${guide.name}/${version}/guide/index.html", guide.title
                    )
                    guide.tags.each {
                        span(
                                style: 'display: none',
                                class: 'tag', it
                        )
                    }
                } else if (multi) {
                    // When a version filter is active (e.g. /versions/8.html), collapse
                    // multi-version rows to a single link for that major so the page
                    // reads as a complete modern catalogue rather than a version picker.
                    if (versionFilter) {
                        a(
                                class: (filteredTags.contains('quick-cast') ? 'quick-cast guide' : 'guide'),
                                href: "$GUIDES_URL/${multiGuide.name}/${versionFilter}/guide/index.html",
                                multiGuide.title
                        )
                        filteredTags.each {
                            span(
                                    style: 'display: none',
                                    class: 'tag', it
                            )
                        }
                    } else {
                        div(class: (guide.tags.contains('quick-cast') ? 'quick-cast multi-guide' : 'multi-guide')) {
                            span(class: 'title', guide.title)
                            // Newest majors first so Grails 8 is the first chip readers see.
                            for (def grailsVersion : versionKeys) {
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
    }

    @CompileDynamic
    static String mainContent(
            List<Guide> guides,
            Set<Tag> tags,
            Category category = null,
            Tag tag = null,
            String version = null
    ) {
        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    if (tag || category || version) {
                        h1 {
                            a(href: '[%url]/index.html', 'Guides')
                            if (tag) {
                                mkp.yield(" → #$tag.title")
                            } else if(category) {
                                mkp.yield(" → $category.name")
                            } else if (version) {
                                mkp.yield(" → Grails $version")
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
                        mkp.yieldUnescaped(rightColumn(tag, category, version, guides))
                    }
                    div(class: 'column') {
                        // leftColumn only renders the clouds (and thus uses the
                        // version list) on the index and version pages; skip the
                        // scan/sort of availableVersions for tag/category pages.
                        def versions = (tag || category) ? [] : GuidesPage.availableVersions(guides)
                        mkp.yieldUnescaped(leftColumn(tag, category, tags, versions, version))
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
                        } else if (!version) {
                            // Version pages render their list in the left column
                            // (see rightColumn); this column only holds the clouds.
                            div(class: 'search-results') {
                                mkp.yieldUnescaped('')
                            }
                        }
                    }
                }
                // Featured modern catalogue: every guide published for the current
                // major, un-capped, sits above the legacy category grid so readers
                // hit Grails 8 first. On version pages the same grid is rendered
                // filtered to that major so /versions/8.html is a full catalogue
                // (flat list in the sidebar + categorized body), not a partial peek.
                if (!(tag || category)) {
                    String featuredVersion = version ?: FEATURED_VERSION
                    List<Guide> featured = guidesForVersionList(featuredVersion, guides)
                    if (featured) {
                        mkp.yieldUnescaped(featuredVersionSection(featuredVersion, featured, version != null))
                        mkp.yieldUnescaped(categoryGrid(guides, featuredVersion, version != null))
                    } else if (!version) {
                        // No featured-version guides yet - fall back to the full
                        // unfiltered category grid so the page is never empty.
                        mkp.yieldUnescaped(categoryGrid(guides, null, false))
                    }
                }
            }
        }
    }

    @CompileDynamic
    static String leftColumn(Tag tag, Category category, Set<Tag> tags, List<String> versions, String version = null) {
        renderHtml {
            div {
                // Clouds stay visible on the index AND on version pages (so a
                // reader on /versions/8 can jump to another version or a tag),
                // but not on tag/category pages where they'd be redundant.
                if (!(tag || category)) {
                    mkp.yieldUnescaped(GuidesPage.versionCloud(versions))
                    mkp.yieldUnescaped(GuidesPage.tagCloud(tags))
                }
            }
        }
    }

    @CompileDynamic
    static String rightColumn(Tag tag, Category category, String version, List<Guide> guides) {
        renderHtml {
            div {
                if (version) {
                    mkp.yieldUnescaped(GuidesPage.guidesForVersion(version, guides))
                } else {
                    mkp.yieldUnescaped(searchBox(tag, category, version))
                    if (!(tag || category)) {
                        mkp.yieldUnescaped(GuidesPage.latestGuides(guides))
                    }
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
     * Renders the tag cloud sidebar. Tags are the primary navigation
     * mechanism on the guides index - clicking a tag lands on a curated page
     * listing every guide carrying that tag. The cloud renders every tag
     * defined in {@code conf/guides.yml} with two filters:
     * <ul>
     *   <li>Version-label tags ({@code grails3}..{@code grails8}) are dropped:
     *       the version is implicit in the guide URL.</li>
     *   <li>Empty-title tags are skipped defensively.</li>
     * </ul>
     * After retagging the registry to a hand-curated taxonomy, every
     * survivor is worth showing, so there is no top-N cap. Tags are
     * sorted alphabetically for display (occurrence still drives the
     * {@code tagN} CSS class so popular tags render larger).
     */
    @CompileDynamic
    static String tagCloud(Set<Tag> tags) {
        List<Tag> curated = tags
                .findAll { Tag t -> t.title && !VERSION_TAG_PATTERN.matcher(t.title).matches() }
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
    static String searchBox(Tag tag, Category category, String version = null) {
        if (!(tag || category || version)) {
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
            String cssStyle = '',
            String versionFilter = null,
            boolean onlyMatchingVersion = false
    ) {
        List<Guide> inCategory = guides.findAll { it.category == category.name }
        if (versionFilter && onlyMatchingVersion) {
            inCategory = inCategory.findAll { guideHasVersion(it, versionFilter) }
        }
        if (!inCategory) {
            return ''
        }
        inCategory = sortGuidesForDisplay(inCategory, versionFilter ?: FEATURED_VERSION)
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
                    inCategory.each {
                        mkp.yieldUnescaped(GuidesPage.renderGuide(it, onlyMatchingVersion ? versionFilter : null))
                    }
                }
            }
        }
    }

    /**
     * Two-column category grid used on the index and on version pages. When
     * {@code onlyMatchingVersion} is true, each category only lists guides that
     * ship the given major (so /versions/8.html surfaces the full Grails 8 set
     * organized by track). When false, every guide stays visible but guides for
     * {@code preferVersion} sort to the top of each category.
     */
    @CompileDynamic
    static String categoryGrid(List<Guide> guides, String preferVersion, boolean onlyMatchingVersion) {
        List<List<Category>> pairs = [
                [categories.apprentice, categories.advanced],
                [categories.weblayer, categories.restapis],
                [categories.devops, categories.gorm],
                [categories.testing, categories.security],
                [categories.spa, categories.async],
                [categories.cloud],
        ]
        StringBuilder html = new StringBuilder()
        boolean firstPair = true
        for (List<Category> pair : pairs) {
            Category leftCat = pair[0]
            Category rightCat = pair.size() > 1 ? pair[1] : null
            String left = leftCat
                    ? guideGroupByCategory(
                    leftCat, guides, true, firstPair ? 'margin-top: 0' : '', preferVersion, onlyMatchingVersion)
                    : ''
            String right = rightCat
                    ? guideGroupByCategory(
                    rightCat, guides, true, firstPair ? 'margin-top: 0' : '', preferVersion, onlyMatchingVersion)
                    : ''
            if (!left && !right) {
                continue
            }
            html << renderHtml {
                div(class: 'two-columns') {
                    div(class: 'column') {
                        if (left) {
                            mkp.yieldUnescaped(left)
                        }
                    }
                    div(class: 'column') {
                        if (right) {
                            mkp.yieldUnescaped(right)
                        }
                    }
                }
            }
            firstPair = false
        }
        html.toString()
    }

    /**
     * Full-width "Grails N Guides" block listing every guide for that major,
     * newest first, with no take() cap. On the index this is the featured modern
     * catalogue; on a version page it sits under the sidebar list as the
     * categorized companion (the sidebar list itself is still rendered by
     * {@link #guidesForVersion}).
     */
    @CompileDynamic
    static String featuredVersionSection(String version, List<Guide> featured, boolean onVersionPage) {
        // On the version page the left column already has the flat list; the
        // body below is the categorized grid only. On the index we lead with
        // this flat "all of them" list so nothing modern is buried.
        if (onVersionPage) {
            return ''
        }
        renderHtml {
            div(class: 'latest-guides featured-version-guides', style: 'margin-top: 0') {
                h3(class: 'column-header', "Grails $version Guides")
                p(style: 'margin: -30px 0 30px 0; padding-left: 28px;') {
                    mkp.yield("Every guide published for Grails $version. ")
                    a(href: "$GUIDES_URL/versions/${version}.html", "Browse by version →")
                }
                ul {
                    featured.each { guide ->
                        li {
                            b(guide.title)
                            span {
                                if (guide.publicationDate) {
                                    mkp.yield(new SimpleDateFormat('MMM dd, yyyy').format(guide.publicationDate))
                                    mkp.yield(' - ')
                                }
                                mkp.yield(guide.category)
                            }
                            a(
                                    href: "$GUIDES_URL/${guide.name}/${version}/guide/index.html",
                                    'Read More'
                            )
                        }
                    }
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

    /**
     * Renders the Grails-version cloud sidebar, sitting directly above the tag
     * cloud (see {@link #tagCloud}). Where tags are a long, free-form taxonomy,
     * the version axis is a short, closed set (the Grails major versions that
     * actually carry guides), so it is the more useful first cut for a reader
     * who only cares about "what can I read for the Grails I'm on". Each entry
     * links to a statically generated {@code /guides/versions/<N>.html} page -
     * exactly mirroring how tag entries link to {@code /guides/tags/<slug>.html}.
     */
    @CompileDynamic
    static String versionCloud(List<String> versions) {
        renderHtml {
            div(class: 'versions-by-grails') {
                h3(class: 'column-header', 'Guides by Grails Version')
                ul(class: 'version-cloud') {
                    versions.each { String v ->
                        li {
                            a(href: "$GUIDES_URL/versions/${v}.html", "Grails $v")
                        }
                    }
                }
            }
        }
    }

    /**
     * Renders every guide published for {@code version} using the same visual
     * treatment as the {@link #latestGuides} list on the index (title, date +
     * category, "Read More"), but with no {@code take()} cap - the version
     * filter is meant to surface the full set, newest first. Each "Read More"
     * link targets the matching version variant of the guide. This list lives
     * in the left column with the version + tag clouds beside it on the right,
     * mirroring the index layout.
     */
    @CompileDynamic
    static String guidesForVersion(String version, List<Guide> guides) {
        List<Guide> matched = guidesForVersionList(version, guides)
        renderHtml {
            div(class: 'latest-guides') {
                h3(class: 'column-header', "Guides for Grails $version")
                if (matched) {
                    p(style: 'margin: -30px 0 30px 0; padding-left: 0;') {
                        mkp.yield("${matched.size()} guide${matched.size() == 1 ? '' : 's'} for Grails $version")
                    }
                }
                ul {
                    matched.each { guide ->
                        li {
                            b(guide.title)
                            span {
                                if (guide.publicationDate) {
                                    mkp.yield(new SimpleDateFormat('MMM dd, yyyy').format(guide.publicationDate))
                                    mkp.yield(' - ')
                                }
                                mkp.yield(guide.category)
                            }
                            a(
                                    href: "$GUIDES_URL/${guide.name}/${version}/guide/index.html",
                                    'Read More'
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Every guide published for {@code version}, newest first, no cap. Shared by
     * the version-page sidebar, the featured index section, and tests.
     */
    static List<Guide> guidesForVersionList(String version, List<Guide> guides) {
        sortGuidesByPublicationDate(
                guides.findAll { Guide guide -> guideHasVersion(guide, version) }
        )
    }

    /**
     * Major-version keys for a multi-version guide, newest first, so the
     * rendered chip row leads with Grails 8 rather than the YAML encounter order.
     */
    static List<Integer> orderedVersionKeys(GrailsVersionedGuide guide) {
        List<Integer> keys = new ArrayList<Integer>(guide.grailsMayorVersionTags.keySet())
        keys.sort { Integer a, Integer b -> b <=> a }
        keys
    }

    /**
     * Sort guides so those that ship {@code preferVersion} come first (newest
     * among themselves), then the rest by publication date descending. Used by
     * the index category grid to keep modern work at the top of each track
     * without hiding the legacy corpus.
     */
    static List<Guide> sortGuidesForDisplay(List<Guide> guides, String preferVersion) {
        List<Guide> preferred = []
        List<Guide> rest = []
        for (Guide guide : guides) {
            if (preferVersion && guideHasVersion(guide, preferVersion)) {
                preferred << guide
            } else {
                rest << guide
            }
        }
        sortGuidesByPublicationDate(preferred) + sortGuidesByPublicationDate(rest)
    }

    static List<Guide> sortGuidesByPublicationDate(List<Guide> guides) {
        List<Guide> sorted = new ArrayList<Guide>(guides)
        sorted.sort { Guide a, Guide b ->
            Date da = a.publicationDate
            Date db = b.publicationDate
            if (da == db) {
                return (a.title ?: '') <=> (b.title ?: '')
            }
            if (da == null) {
                return 1
            }
            if (db == null) {
                return -1
            }
            int byDate = db <=> da
            byDate != 0 ? byDate : (a.title ?: '') <=> (b.title ?: '')
        }
        sorted
    }

    /**
     * The distinct Grails major versions across all guides, newest first.
     * A {@link SingleGuide} contributes its single {@code versionNumber}; a
     * {@link GrailsVersionedGuide} contributes every key in
     * {@code grailsMayorVersionTags}. Versions are guaranteed numeric major
     * versions in {@code conf/guides.yml}, so they sort numerically descending.
     */
    static List<String> availableVersions(List<Guide> guides) {
        Set<String> versions = [] as Set<String>
        for (Guide guide : guides) {
            if (guide instanceof GrailsVersionedGuide) {
                for (Integer v : ((GrailsVersionedGuide) guide).grailsMayorVersionTags.keySet()) {
                    versions.add(v.toString())
                }
            } else if (guide.versionNumber) {
                versions.add(guide.versionNumber)
            }
        }
        List<String> list = new ArrayList<String>(versions)
        list.sort { String a, String b -> (b as Integer) <=> (a as Integer) }
        list
    }

    /** Whether {@code guide} is published for the given Grails major version. */
    static boolean guideHasVersion(Guide guide, String version) {
        if (guide instanceof GrailsVersionedGuide) {
            for (Integer v : ((GrailsVersionedGuide) guide).grailsMayorVersionTags.keySet()) {
                if (v.toString() == version) {
                    return true
                }
            }
            return false
        }
        guide.versionNumber == version
    }
}

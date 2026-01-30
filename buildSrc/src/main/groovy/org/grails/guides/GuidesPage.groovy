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
package org.grails.guides

import java.text.SimpleDateFormat

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

import org.grails.tags.Tag
import org.grails.utils.ReadFileUtils

@CompileStatic
class GuidesPage {

    public static final Integer NUMBER_OF_LATEST_GUIDES = 8
    public static final String GUIDES_URL = 'https://guides.grails.org'

    static Map<String, Category> categories = [
            advanced: new Category(name: 'Advanced Grails', image: 'advancedgrails.svg'),
            android: new Category(name: 'Grails + Android', image: 'grails_android.svg'),
            angular: new Category(name: 'Grails + Angular', image: 'grailsangular.svg'),
            angularjs: new Category(name: 'Grails + AngularJS', image: 'grailsangular.svg'),
            apprentice: new Category(name: 'Grails Apprentice', image: 'grailaprrentice.svg'),
            async: new Category(name: 'Grails Async', image: 'async.svg'),
            devops: new Category(name: 'Grails + DevOps', image: 'grailsdevops.svg'),
            googlecloud: new Category(name: 'Grails + Google Cloud', image: 'googlecloud.svg'),
            gorm: new Category(name: 'GORM', image: 'gorm.svg'),
            ios: new Category(name: 'Grails + iOS', image: 'ios.svg'),
            react: new Category(name: 'Grails + React', image: 'react.svg'),
            ria: new Category(name: 'Grails + RIA (Rich Internet Application)', image: 'ria.svg'),
            testing: new Category(name: 'Grails Testing', image: 'testing.svg'),
            vue: new Category(name: 'Grails + Vue.js', image: 'vue.svg'),
    ]
    
    
    @CompileDynamic
    static String renderGuide(Guide guide) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            li {
                if (guide instanceof SingleGuide) {
                    a(
                            class: (guide.tags.contains('quickcast') ? 'quickcast guide' : 'guide'),
                            href: "$GUIDES_URL/$guide.name/guide/index.html", guide.title
                    )
                    guide.tags.each {
                        span(
                                style: 'display: none',
                                class: 'tag', it
                        )
                    }
                } else if (guide instanceof GrailsVersionedGuide) {
                    def multiGuide = (GrailsVersionedGuide) guide
                    div(class: (guide.tags.contains('quickcast') ? 'quickcast multiguide' : 'multiguide')) {
                        span(class: 'title', guide.title)
                        for (def grailsVersion :  multiGuide.grailsMayorVersionTags.keySet())  {
                            def tagList = multiGuide.grailsMayorVersionTags[grailsVersion] as Set<String>
                            div(class: 'align-left') {
                                a(
                                        class: 'grailsVersion',
                                        href: "$GUIDES_URL/${grailsVersion.name().toLowerCase().replaceAll('_', '')}/${multiGuide.githubSlug.replaceAll(/grails-guides\//, '')}/guide/index.html"
                                ) {
                                    mkp.yield(grailsVersion.name().replaceAll('_', ' '))
                                }
                                tagList.each {
                                    span(
                                            style: 'display: none',
                                            class: 'tag', it
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String mainContent(
            ClassLoader classLoader,
            List<Guide> guides,
            Set<Tag> tags,
            Category category = null,
            Tag tag = null
    ) {
        StringWriter writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'headerbar chalicesbg') {
                div(class: 'content') {
                    if (tag || category) {
                        h1 {
                            a(href: "[%url]/index.html", 'Guides')
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
                div(class: 'twocolumns') {
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
                            div(class: 'searchresults') {
                                mkp.yieldUnescaped('')
                            }
                        }
                    }
                }
                div(class: 'twocolumns') {
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

                div(class: 'twocolumns') {
                    div(class: 'column') {
                        if (!(tag || category)) {
                            mkp.yieldUnescaped(guideGroupByCategory(categories.gorm, guides, true, 'margin-top: 0;'))
                        }
                    }
                    div(class: 'column') {
                        if ( !(tag || category) ) {
                            mkp.yieldUnescaped guideGroupByCategory(categories.testing, guides, true, 'margin-top: 0;')

                        }
                    }
                }
                div(class: 'twocolumns') {
                    div(class: 'column') {
                        if ( !(tag || category) ) {
                            mkp.yieldUnescaped guideGroupByCategory(categories.devops, guides, true, 'margin-top: 0;')
                            mkp.yieldUnescaped guideGroupByCategory(categories.googlecloud, guides)
                            mkp.yieldUnescaped guideGroupByCategory(categories.ios, guides)
                            mkp.yieldUnescaped guideGroupByCategory(categories.android, guides)
                            mkp.yieldUnescaped guideGroupByCategory(categories.ria, guides)
                        }
                    }
                    div(class: 'column') {
                        if ( !(tag || category) ) {
                            mkp.yieldUnescaped guideGroupByCategory(categories.vue, guides, true, 'margin-top: 0;')
                            mkp.yieldUnescaped guideGroupByCategory(categories.angular, guides, true, 'margin-top: 0;')
                            mkp.yieldUnescaped guideGroupByCategory(categories.angularjs, guides)
                            mkp.yieldUnescaped guideGroupByCategory(categories.react, guides)
                            mkp.yieldUnescaped guideSuggestion(classLoader)
                        }
                    }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String guideSuggestion(ClassLoader classLoader) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'guidesuggestion') {
                h3(class: 'columnheader', 'Which topic would you like us to cover?')
                def formHtml = ReadFileUtils.readFileContent(classLoader, 'guidesuggestionform.html')
                if (formHtml) {
                    mkp.yieldUnescaped(formHtml)
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String leftColumn(Tag tag, Category category, Set<Tag> tags) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div {
                if (!(tag || category)) {
                    mkp.yieldUnescaped(tagCloud(tags))
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String rightColumn(Tag tag, Category category, List<Guide> guides) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div {
                mkp.yieldUnescaped(searchBox(tag, category))
                if (!(tag || category)) {
                    mkp.yieldUnescaped(latestGuides(guides))
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String latestGuides(List<Guide> guides) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'latestguides') {
                h3(class: 'columnheader', 'Latest Guides')
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
                                    a(href: "$GUIDES_URL/$guide.name/guide/index.html", 'Read More')
                                }
                            }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String tagCloud(Set<Tag> tags) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'tagsbytopic') {
                h3(class: 'columnheader', 'Guides by Tag')
                ul(class: 'tagcloud') {
                    tags.sort { it.slug }.each { tag ->
                        li(class: "tag$tag.ocurrence") {
                            a(href: "$GUIDES_URL/tags/${tag.slug.toLowerCase()}.html", tag.title)
                        }
                    }
                }
            }
        }
        writer.toString()
    }


    @CompileDynamic
    static String searchBox(Tag tag, Category category) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            if (!(tag || category)) {
                div(class: 'searchbox', style: 'margin-top: 50px !important') {
                    div(class: 'search', style: 'margin-bottom: 0px !important') {
                        input(type: 'text', id: 'query', placeholder: 'SEARCH')
                    }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String guideGroupByCategory(
            Category category,
            List<Guide> guides,
            boolean linkToCategory = true,
            String cssStyle = ''
    ) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'guidegroup', style: cssStyle) {
                div(class: 'guidegroupheader') {
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
                            .each { mkp.yieldUnescaped(renderGuide(it)) }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String guideGroupByTag(Tag tag, List<Guide> guides) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'guidegroup') {
                div(class: 'guidegroupheader') {
                    img(src: '[%url]/images/documentation.svg', alt: 'Guides')
                    h2("Guides filtered by #$tag.title")
                }
                ul {
                    guides
                            .findAll { Guide guide -> guide.tags.contains(tag.title) }
                            .each { mkp.yieldUnescaped(renderGuide(it)) }
                }
            }
        }
        writer.toString()
    }
}

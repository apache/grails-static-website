package org.grails.documentation

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import org.yaml.snakeyaml.Yaml

@CompileStatic
class DocumentationPage {

    @CompileDynamic
    private static String renderCategory(DocumentationCategory cat) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div(class: 'guidegroup') {
            html.div(class: 'guidegroupheader') {
                html.img(src: cat.image, alt: cat.title)
                h2 cat.title
            }
            ul {
                cat.items.each { item ->
                    li {
                        a(href: item.url, item.title)
                    }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    static String renderDocumentation(String version) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        html.div(class: "guidegroup") {
            if (version) {
                boolean isSnapshot = version.endsWith('-SNAPSHOT') || version.contains('snapshot')
                div(class: "guidegroupheader") {
                    img(src: "[%url]/images/documentation.svg", alt: "Grails Version (${version})")
                    h2 "${isSnapshot ? 'Snapshot' : (version.contains('.M') ? 'Milestone' : (version.contains('.RC') ? 'Release Candidate': 'Latest'))} Version (${version}) Documentation"
                }
                ul {
                    li {
                        a(href: "https://docs.grails.org/${version}/", 'User Guide')
                    }
                    li {
                        a(href: "https://docs.grails.org/${version}/api/", 'API Reference')
                    }
                }
            }
        }
        writer.toString()
    }


    @CompileDynamic
    static String mainContent(File releases, File modules) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)

        html.div(class: 'headerbar chalicesbg') {
            html.div(class: 'content') {
                h1 'Documentation'
            }
        }
        SoftwareVersion preRelease = SiteMap.latestPreReleaseVersion(releases)
        SoftwareVersion latest = SiteMap.latestVersion(releases)
        Collection<DocumentationCategory> categories = categories(modules)
        html.div(class: 'content') {
            html.div(class: "twocolumns") {
                html.div(class: "odd column"){
                    if (preRelease > latest) {
                        mkp.yieldUnescaped(renderDocumentation(preRelease.versionText))
                    }

                    mkp.yieldUnescaped(renderDocumentation('snapshot'))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'GORM - Data Access Toolkit' }))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Security' }))

                }
                html.div(class: "column") {
                    mkp.yieldUnescaped(renderDocumentation(latest.versionText))

                    html.div(class: 'olderversions') {
                        h3(class: 'columnheader', style: 'margin-bottom: 10px;', 'Older Version')
                        p 'Browse previous versions\' documentation since Grails 1.2.0'

                        div(class: 'versionselector') {
                            h4 'Single Page - User Guide'
                            select(onchange: "window.location.href='https://docs.grails.org/' + this.value + '/'") {
                                option 'Select a version'
                                mkp.yield('[%versions]')
                            }
                        }
                        div(class: 'versionselector') {
                            h4 'User Guide'
                            select(onchange: "window.location.href='https://docs.grails.org/' + this.value") {
                                option 'Select a version'
                                mkp.yield('[%versions]')
                            }
                        }
                        div(class: 'versionselector') {
                            h4 'API Reference'
                            select(onchange: "window.location.href='https://docs.grails.org/' + this.value + '/api'") {
                                option 'Select a version'
                                mkp.yield('[%versions]')
                            }
                        }
                    }

                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Upgrade' }))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Testing' }))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Views' }))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Async' }))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Database' }))
                    mkp.yieldUnescaped(renderCategory(categories.find { it.title == 'Redis' }))
                }

            }
        }

        writer.toString()
    }

    @CompileDynamic
    private static Collection<DocumentationCategory> categories(File modules) {
        Yaml yaml = new Yaml()
        Map model = yaml.load(modules.newDataInputStream())
        Map<String, DocumentationCategory> categories = [:]
        model['modules'].collect { k, v ->
            if (!categories.containsKey(v.category)) {
                DocumentationCategory cat = new DocumentationCategory()
                if (v.category) {
                    cat.title = v.category
                }
                if (v.categoryImage) {
                    cat.image = v.categoryImage
                }
                if (v.categoryDescription) {
                    cat.description = v.categoryDescription
                }
                if (v.categoryUrl) {
                    cat.url = v.categoryUrl
                }
                categories[v.category] = cat
            }
            DocumentationItem item = new DocumentationItem()
            item.url = v.url
            item.title = v.title
            categories[v.category].items << item
        }
        categories.values()
    }

    static class DocumentationCategory {
        String title
        String image
        String description
        String url
        List<DocumentationItem> items = []
    }

    static class DocumentationItem {
        String title
        String url
    }
}

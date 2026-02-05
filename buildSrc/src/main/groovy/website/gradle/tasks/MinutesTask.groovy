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
package website.gradle.tasks

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

import javax.inject.Inject

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

import io.micronaut.rss.DefaultRssFeedRenderer
import io.micronaut.rss.RssChannel
import io.micronaut.rss.RssItem
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import website.gradle.GrailsWebsiteExtension
import website.model.HtmlMinutes
import website.model.MarkdownMinutes
import website.model.MinutesMetadataAdaptor
import website.model.documentation.SiteMap
import website.utils.DateUtils
import website.utils.MarkdownUtils

import static website.utils.RenderUtils.renderHtml

@CompileStatic
@CacheableTask
abstract class MinutesTask extends GrailsWebsiteTask {

    @Internal
    final String description =
            'Renders Markdown minutes (minutes/*.md) into HTML pages (dist/foundation/minutes/*.html). ' +
            'It generates tag pages. Generates RSS feed. Minutes with future dates are not generated.'

    public static final String NAME = 'renderMinutes'

    private static final int MAX_TITLE_LENGTH = 45
    private static final String RSS_FILE = 'minutes.xml'
    private static final String IMAGES = 'images'
    private static final String MINUTES_BG = 'grails-blog-index-6.png'
    private static final String MINUTES = 'foundation/minutes'
    private static final String INDEX = 'index.html'

    @Inject
    abstract FileSystemOperations getFileSystemOperations()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getDocument()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getReleases()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getAssetsDir()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getMinutesDir()

    @Input
    abstract Property<String> getAbout()

    @Input
    abstract ListProperty<String> getKeywords()

    @Input
    abstract Property<String> getRobots()

    @Input
    abstract Property<String> getTitle()

    @Input
    abstract Property<String> getUrl()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    static TaskProvider<MinutesTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, MinutesTask) {
            it.about.set(siteExt.description)
            it.assetsDir.set(siteExt.assetsDir)
            it.document.set(siteExt.template)
            it.keywords.set(siteExt.keywords)
            it.minutesDir.set(siteExt.minutesDir)
            it.outputDir.set(siteExt.outputDir)
            it.releases.set(siteExt.releases)
            it.robots.set(siteExt.robots)
            it.title.set(siteExt.title)
            it.url.set(siteExt.url)
        }
    }

    @TaskAction
    void renderMinutes() {
        def meta = RenderSiteTask.siteMeta(
                title.get(),
                about.get(),
                url.get(),
                keywords.get() as List<String>,
                robots.get(),
                SiteMap.latestVersion(releases.get().asFile).versionText,
                SiteMap.olderVersions(releases.get().asFile)
                        .reverse()
                        .collect {"<option>$it</option>" }
                        .join(' ')
        )
        renderMinutes(
                meta,
                processMinutes(meta, parseMinutes(minutesDir.get().asFile).sort(false)),
                outputDir.dir('foundation/minutes').get().asFile.tap { it.mkdirs() },
                document.get().asFile.text
        )
        fileSystemOperations.copy { CopySpec copy ->
            copy.from(assetsDir.dir('bgimages'))
            copy.into(outputDir.dir('dist/images'))
            copy.include(AssetsTask.assetTypes.images)
        }
    }

    @CompileDynamic
    static String renderMinutesHtml(
            HtmlMinutes htmlMinutes,
            String templateText,
            List<HtmlMinutes> minutes
    ) {

        def groupedMinutes = minutes.groupBy {
            DateUtils.parseDate(it.metadata.date)[Calendar.YEAR]
        }

        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    h1 {
                        a(href: '[%url]/foundation/index.html', 'Foundation')
                    }
                }
            }

            article(class: 'content container') {
                section(class: 'large-golden-ratio align-left foundation-description') {
                    div {
                        mkp.yieldUnescaped(htmlMinutes.html)
                    }
                }

                section(class: 'small-golden-ratio align-left foundation-boards') {
                    div(class: 'meeting-archive-list') {
                        a(href: '[%url]/foundation/minutes/index.html', style: 'text-decoration: none', title: 'Meeting Minutes Archive') {
                            h2 { mkp.yield("Meeting Minutes Archive") }
                        }
                        br()
                        div {
                            groupedMinutes.each { year, yearMinutes ->
                                h3 { mkp.yield(year) }
                                ul {
                                    yearMinutes.each { m ->
                                        li {
                                            a(href: minutesLink(m), "${parseDate(m.metadata.date).format("MMM dd")} - ${m.metadata.title}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        def html = writer.toString()
        def metadata = htmlMinutes.metadata.toMap()
        html = RenderSiteTask.renderHtmlWithTemplateContent(html, metadata, templateText)
        html = RenderSiteTask.highlightMenu(html, metadata, htmlMinutes.path)
        metadata['bodyClassAttr'] = metadata['bodyClassAttr'] ?: 'foundation minutes'
        html = html.replace('<body>', "<body class='${metadata['bodyClassAttr']}'>")
        html
    }

    static List<HtmlMinutes> processMinutes(
            Map<String, String> globalMetadata,
            List<MarkdownMinutes> markdownMinutes
    ) {
        markdownMinutes.collect { mdMinutes ->
            def metadata = RenderSiteTask.processMetadata(
                    globalMetadata + mdMinutes.metadata
            )
            def minutesMetadata = new MinutesMetadataAdaptor(metadata)
            def markdown = mdMinutes.content
            if (metadata.containsKey('slides')) {
                markdown = markdown + "\n\n[Slides](${metadata['slides']})\n\n"
            }
            if (metadata.containsKey('code')) {
                markdown = markdown + "\n\n[Code](${metadata['code']})\n\n"
            }
            def html = MarkdownUtils.htmlFromMarkdown(markdown)
            def iframe = RenderSiteTask.parseVideoIframe(metadata)
            if (iframe) {
                html = html + iframe
            }
            new HtmlMinutes(
                    metadata: minutesMetadata,
                    html: html,
                    path: mdMinutes.path
            )
        }
    }

    static void renderMinutes(
            Map<String, String> globalMetadata,
            List<HtmlMinutes> listOfMinutes,
            File outputDir,
            String templateText
    ) {
        List<String> minuteCards = []
        List<RssItem> rssItems = []

        for (def htmlMinutes : listOfMinutes) {
            minuteCards.add(minutesCard(htmlMinutes))
            new File(outputDir, htmlMinutes.path).tap {
                it.text = renderMinutesHtml(htmlMinutes, templateText, listOfMinutes)
            }
            rssItems.add(
                    BlogTask.rssItemWithPage(
                            htmlMinutes.metadata.title,
                            DateUtils.parseDate(htmlMinutes.metadata.date),
                            minutesLink(htmlMinutes),
                            htmlMinutes.path.replace('.html', ''),
                            htmlMinutes.html
                    )
            )
        }
        renderArchive(
                new File(outputDir, 'index.html'),
                minuteCards,
                globalMetadata,
                templateText
        )
        renderRss(
                globalMetadata,
                rssItems,
                new File(outputDir.parentFile, RSS_FILE)
        )
    }

    static String minutesLink(HtmlMinutes minutes) {
        "$minutes.metadata.url/$MINUTES/$minutes.path"
    }

    @CompileDynamic
    private static String minutesCard(HtmlMinutes htmlMinutes) {
        renderHtml {
            article(class: 'blog-card', style: "margin-bottom: 0; background-image: url($htmlMinutes.metadata.url/$IMAGES/$MINUTES_BG)") {
                a(href: minutesLink(htmlMinutes)) {
                    h3 {
                        mkp.yield(RenderSiteTask.formatDate(htmlMinutes.metadata.date))
                    }
                    h2 {
                        def title = htmlMinutes.metadata.title
                        if (title.length() > MAX_TITLE_LENGTH) {
                            title = "${title.substring(0, MAX_TITLE_LENGTH)}..."
                        }
                        mkp.yield(title)
                    }
                }
            }
        }
    }

    private static void renderArchive(
            File f,
            List<String> minuteCards,
            Map<String, String> siteMeta,
            String templateText
    ) {
        def html = cardsHtml(minuteCards.toList())
        def resolvedMetadata = RenderSiteTask.processMetadata(siteMeta).tap {
            it.title = 'Foundation | Grails Framework'
        }
        html = RenderSiteTask.renderHtmlWithTemplateContent(html, resolvedMetadata, templateText)
        html = RenderSiteTask.highlightMenu(html, resolvedMetadata, "/$MINUTES/$INDEX")
        f.text = html
    }

    @CompileDynamic
    static String cardsHtml(List<String> cards, String title = null) {
        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    if (title) {
                        mkp.yieldUnescaped(title)
                    } else {
                        h1 {
                            a(href: '[%url]/foundation/index.html', 'Foundation')
                        }
                    }
                }
            }
            div(class: 'clear content container') {
                h3(class: 'column-header', 'Meeting Minutes Archive')
                div(class: 'light') {
                    div(class: 'padded', style: 'padding-top: 0;') {
                        for (int i = 0; i < cards.size(); i++) {
                            if (i == 0) {
                                mkp.yieldUnescaped('<div class="three-columns">')
                            }
                            div(class: 'column') {
                                mkp.yieldUnescaped(cards[i])
                            }

                            if (i != 0 && ((i + 1) % 3 == 0)) {
                                mkp.yieldUnescaped('</div>')
                                if (i != (cards.size() - 1)) {
                                    mkp.yieldUnescaped('<div class="three-columns">')
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void renderRss(
            Map<String, String> siteMeta,
            List<RssItem> rssItems,
            File outputFile
    ) {
        def builder = RssChannel.builder(
                siteMeta['title'],
                siteMeta['url'],
                siteMeta['description']
        )
        builder.pubDate(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of('GMT')))
        builder.lastBuildDate(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of('GMT')))
                .docs('https://blogs.law.harvard.edu/tech/rss')
                .generator('Micronaut RSS')
                .managingEditor('private@grails.apache.org')
                .webMaster('private@grails.apache.org')
        rssItems.each { builder.item(it) }
        def writer = new FileWriter(outputFile)
        new DefaultRssFeedRenderer().with {
            render(writer, builder.build())
        }
        writer.close()
    }

    static List<MarkdownMinutes> parseMinutes(File minutes) {
        List<MarkdownMinutes> listOfMinutes = []
        minutes.eachFile { file ->
            if (file.path.endsWith('.md') || file.path.endsWith('.markdown')) {
                def contentAndMetadata = RenderSiteTask.parseFile(file)
                listOfMinutes.add(new MarkdownMinutes(
                        filename: file.name,
                        content: contentAndMetadata.content,
                        metadata: contentAndMetadata.metadata
                ))
            }
        }
        listOfMinutes
    }
}

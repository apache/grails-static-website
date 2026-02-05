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

import jakarta.annotation.Nonnull
import javax.inject.Inject
import jakarta.validation.constraints.NotNull

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import jakarta.annotation.Nullable
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
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

import website.model.ContentAndMetadata
import website.utils.DateUtils
import website.model.Page
import website.model.documentation.SiteMap
import website.gradle.GrailsWebsiteExtension

import static groovy.io.FileType.FILES

@CompileStatic
@CacheableTask
class RenderSiteTask extends GrailsWebsiteTask {

    @Internal
    final String description =
            'Build Grails website - generates pages with HTML entries in pages and build/temp, ' +
            'renders blog and RSS feed, copies assets and generates a sitemap'

    public static final String NAME = 'renderSite'

    private static final String YOUTUBE_WATCH = 'https://www.youtube.com/watch?v='
    private static final String COLON = ':'
    private static final String SEPARATOR = '---'
    private static final int TWITTER_CARD_PLAYER_WIDTH = 560
    private static final int TWITTER_CARD_PLAYER_HEIGHT = 315

    private final ObjectFactory objects

    @Inject
    RenderSiteTask(ObjectFactory objects) {
        this.objects = objects
    }
    
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty document = objects.fileProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty releases = objects.fileProperty()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty pagesDir = objects.directoryProperty()

    @Input
    final Property<String> title = objects.property(String)

    @Input
    final Property<String> about = objects.property(String)

    @Input
    final Property<String> url = objects.property(String)

    @Input
    final ListProperty<String> keywords = objects.listProperty(String)

    @Input
    final Property<String> robots = objects.property(String)

    @OutputDirectory
    final DirectoryProperty outputDir = objects.directoryProperty()

    static TaskProvider<RenderSiteTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, RenderSiteTask) {
            it.about.set(siteExt.description)
            it.document.set(siteExt.template)
            it.keywords.set(siteExt.keywords)
            it.outputDir.set(siteExt.outputDir)
            it.pagesDir.set(siteExt.pagesDir)
            it.releases.set(siteExt.releases)
            it.robots.set(siteExt.robots)
            it.title.set(siteExt.title)
            it.url.set(siteExt.url)
        }
    }

    @TaskAction
    void renderSite() {
        def o = outputDir.get().asFile
        def releasesFile = releases.get().asFile
        def latest = SiteMap.latestVersion(releasesFile)
        def versions = SiteMap.olderVersions(releasesFile)
                .reverse()
                .collect { "<option>${it}</option>" }
                .join(' ')
        def m = website.gradle.tasks.RenderSiteTask.siteMeta(
                title.get(),
                about.get(),
                url.get(),
                keywords.get(),
                robots.get(),
                latest.versionText,
                versions)
        def listOfPages = parsePages(pagesDir.get().asFile)
        listOfPages.addAll(
                parsePages(
                        new File(o, 'temp')
                )
        )
        File dist = new File(o, 'dist')
        renderPages(m, listOfPages, dist, document.get().asFile.text)
    }

    static Map<String, String> siteMeta(
            String title,
            String about,
            String url,
            List<String> keywords,
            String robots,
            String latest,
            String versionsBeforeGrails6
    ) {
        return [
            title: title,
            description: about,
            url: url,
            latest: latest,
            events: '',
            versions: versionsBeforeGrails6,
            versionAfterGrails6: versionsBeforeGrails6,
            keywords: keywords.join(','),
            robots: robots,
        ]
    }

    static void renderPages(
            Map<String,
            String> siteMeta,
            List<Page> listOfPages,
            File outputDir,
            String templateText
    ) {
        for (def page : listOfPages) {
            def resolvedMetadata = processMetadata(
                    siteMeta + page.metadata
            )
            def html = renderHtmlWithTemplateContent(
                    page.content,
                    resolvedMetadata,
                    templateText
            )
            html = highlightMenu(html, siteMeta, page.path)
            if (page.bodyClassAttr) {
                html = html.replace('<body>', "<body class='${page.bodyClassAttr}'>")
            }
            saveHtmlToPath(outputDir, html, page.path)
        }
    }

    static void saveHtmlToPath(File outputDir, String html, String filepath) {
        def pageOutput = new File(outputDir.absolutePath).tap { it.mkdirs() }
        def paths = filepath.split('/')
        for (String path : paths) {
            if (path.endsWith('.html')) {
                pageOutput = new File(pageOutput, path)
            } else if (path.trim().isEmpty()) {
                // continue
            } else {
                pageOutput = new File(pageOutput, path).tap { it.mkdirs() }
            }
        }
        pageOutput.setText(html, 'UTF-8')
    }

    static Map<String, String> processMetadata(Map<String, String> siteMeta) {
        def resolvedMetadata = siteMeta
        if (resolvedMetadata.containsKey('CSS')) {
            resolvedMetadata.put(
                    'CSS',
                    "<link rel='stylesheet' href='[%url]" + resolvedMetadata['CSS'] + "'/>"
            )
        } else {
            resolvedMetadata.put('CSS', '')
        }
        if (resolvedMetadata.containsKey('JAVASCRIPT')) {
            resolvedMetadata.put(
                    'JAVASCRIPT',
                    "<script src='" + resolvedMetadata['JAVASCRIPT'] + "'></script>"
            )
        } else {
            resolvedMetadata.put('JAVASCRIPT', '')
        }
        if (!resolvedMetadata.containsKey('HTML header')) {
            resolvedMetadata.put('HTML header', '')
        }
        if (!resolvedMetadata.containsKey('keywords')) {
            resolvedMetadata.put('keywords', '')
        }
        if (!resolvedMetadata.containsKey('description')) {
            resolvedMetadata.put('description', '')
        }
        if (!resolvedMetadata.containsKey('date')) {
            resolvedMetadata.put(
                    'date',
                    DateUtils.format_MMM_D_YYYY_HHMM(new Date())
            )
        }
        if (!resolvedMetadata.containsKey('robots')) {
            resolvedMetadata.put('robots', 'all')
        }
        resolvedMetadata.put(
                'twittercard',
                twitterCard('summary_large_image')
        )
        if (resolvedMetadata.containsKey('video')) {
            def videoId = parseVideoId(resolvedMetadata)
            if (videoId) {
                resolvedMetadata.put(
                        'twittercard',
                        twitterCard('player') +
                                twitterPlayerHtml(
                                        videoId,
                                        TWITTER_CARD_PLAYER_WIDTH,
                                        TWITTER_CARD_PLAYER_HEIGHT
                                )
                )
            }
        }
        resolvedMetadata
    }

    @Nullable
    static String parseVideoId(Map<String, String> metadata) {
        metadata.containsKey('video') && metadata['video'].startsWith(YOUTUBE_WATCH) ?
                metadata['video'].substring(YOUTUBE_WATCH.length()) :
                null
    }

    @Nullable
    static String parseVideoIframe(Map<String, String> metadata) {
        def videoId = parseVideoId(metadata)
        videoId ?
                '<iframe width="100%" height="560" src="https://www.youtube-nocookie.com/embed/' + videoId + '" frameborder="0"></iframe>' :
                null
    }

    static String twitterPlayerHtml(String videoId, int width, int height) {
        """\
        <meta name='twitter:player' content='https://www.youtube.com/embed/$videoId'/>
        <meta name='twitter:player:width' content='$width'/>
        <meta name='twitter:player:height' content='$height'/>
        """.stripIndent(8)
    }

    static String twitterCard(String cardType) {
        "<meta name='twitter:card' content='$cardType'/>"
    }

    static String highlightMenu(String html, Map<String, String> sitemeta, String path) {
        html.replaceAll(
                "<li><a href='" + sitemeta['url'] + path,
                "<li class='active'><a href='" + sitemeta['url'] + path
        )
    }

    static List<Page> parsePages(File pages) {
        List<Page> listOfPages = []
        pages.eachFileRecurse(FILES) { file ->
            if (file.path.endsWith('.html')) {
                def contentAndMetadata = parseFile(file)
                def filename = file.absolutePath.replace(pages.absolutePath, '')
                listOfPages.add(
                        new Page(
                                filename: filename,
                                content: contentAndMetadata.content,
                                metadata: contentAndMetadata.metadata
                        )
                )
            }
        }
        listOfPages
    }

    static ContentAndMetadata parseFile(File file) {
        String line = null
        List<String> lines = []
        Map<String, String> metadata = [:]
        boolean metadataProcessed = false
        int lineCount = 0
        file.withReader { reader ->
            while ((line = reader.readLine()) != null) {
                if (lineCount == 0 && line.startsWith(SEPARATOR)) {
                    continue
                }
                lineCount++
                if (line.startsWith(SEPARATOR)) {
                    metadataProcessed = true
                    continue
                }
                if (!metadataProcessed && line.contains(COLON)) {
                    String metadataKey = line.substring(0, line.indexOf(COLON as String)).trim()
                    String metadataValue = line.substring(line.indexOf(COLON as String) + COLON.length()).trim()
                    metadata[metadataKey] = metadataValue
                }
                line = replaceLineWithMetadata(line, metadata)
                if (metadataProcessed) {
                    lines << line
                }
            }
        }

        !metadataProcessed || lines.isEmpty() ?
                new ContentAndMetadata(metadata: [:] as Map<String, String>, content: file.text) :
                new ContentAndMetadata(metadata: metadata, content: lines.join('\n'))
    }

    @Nonnull
    static String renderHtmlWithTemplateContent(
            @Nonnull @NotNull String html,
            @Nonnull @NotNull Map<String, String> meta,
            @NotNull @Nonnull String templateText
    ) {
        def outputHtml = templateText
        def result = outputHtml.replace(' data-document>', '>' + html)
        return replaceLineWithMetadata(result, meta)
    }

    static String formatDate(String date) {
        DateUtils.format_MMMM_D_YYYY(
                DateUtils.parseDate(date)
        )
    }

    @CompileDynamic
    static String formatDateMinus6Months(String date) {
        use(TimeCategory) {
            DateUtils.format_MMMM_D_YYYY(
                    DateUtils.parseDate(date) - 6.months
            )
        }
    }

    static String URLEncode(String date) {
        URLEncoder.encode(date, 'UTF-8')
    }

    static String replaceLineWithMetadata(String line, Map<String, String> metadata) {
        Map<String, String> m = new HashMap<>(metadata)
        if (m.containsKey('date')) {
            m['date'] = formatDate(m['date'])
            m['6MonthsBackForGitHub'] =
                    '?from=' +
                            URLEncode(formatDateMinus6Months(m['date'])) +
                            '&to=' +
                            URLEncode(formatDate(m['date']))
        }
        for (String metadataKey : m.keySet()) {
            if (line.contains("[%$metadataKey]")) {
                def value = m[metadataKey]
                if ("[%$metadataKey]" == '[%author]') {
                    def authors = value.split(',') as List<String>
                    value = '<span class="author">By ' + authors.join('<br/>') + '</span>'
                    line = line.replaceAll(
                            "\\[%$metadataKey\\]",
                            value
                    )

                } else if ("[%$metadataKey]" == '[%date]') {
                    if (line.contains('<meta')) {
                        line = line.replaceAll(
                                "\\[%$metadataKey\\]",
                                value
                        )
                    } else {
                        value = '<span class="date">' + value + '</span>'
                        line = line.replaceAll(
                                "\\[%$metadataKey\\]",
                                value
                        )
                    }
                } else {
                    line = line.replaceAll(
                            "\\[%$metadataKey\\]",
                            value
                    )
                }
            }
        }
        line
    }
}

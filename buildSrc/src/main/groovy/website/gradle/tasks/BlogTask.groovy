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

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.regex.Pattern

import jakarta.annotation.Nonnull
import javax.inject.Inject

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

import io.micronaut.rss.DefaultRssFeedRenderer
import io.micronaut.rss.RssChannel
import io.micronaut.rss.RssItem
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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

import website.model.HtmlPost
import website.model.MarkdownPost
import website.model.PostMetadataAdapter
import website.model.documentation.SiteMap
import website.gradle.GrailsWebsiteExtension
import website.utils.DateUtils
import website.utils.MarkdownUtils

@CompileStatic
@CacheableTask
class BlogTask extends GrailsWebsiteTask {

    @Internal
    final String description =
            'Renders Markdown posts (posts/*.md) into HTML pages (dist/blog/*.html). ' +
            'It generates tag pages. Generates RSS feed. ' +
            'Posts with future dates are not generated.'

    public static final String NAME = 'renderBlog'

    private static final String BLOG = 'blog'
    private static final String IMAGES = 'images'
    private static final String INDEX = 'index.html'
    private static final String RSS_FILE = 'rss.xml'
    private static final String TAG = 'tag'

    private static final Pattern HASHTAG_SPAN = ~/<span class="hashtag">#(.*?)<\/span>/

    private static List<String> ALLOWED_TAG_PREFIXES =
            (('A'..'Z') + ('a'..'z') + ('0'..'9')).collect { '#' + it }

    private static final int MAX_RELATED_POSTS = 3
    private static final int MAX_TITLE_LENGTH = 45

    private final ObjectFactory objects
    private final FileSystemOperations fileSystemOperations

    @Inject
    BlogTask(ObjectFactory objects, FileSystemOperations fileSystemOperations) {
        this.objects = objects
        this.fileSystemOperations = fileSystemOperations
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty document = objects.fileProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty releases = objects.fileProperty()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty assetsDir = objects.directoryProperty()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty postsDir = objects.directoryProperty()

    @Input
    final Property<String> about = objects.property(String)

    @Input
    final ListProperty<String> keywords = objects.listProperty(String)

    @Input
    final Property<String> robots = objects.property(String)

    @Input
    final Property<String> title = objects.property(String)

    @Input
    final Property<String> url = objects.property(String)

    @OutputDirectory
    final DirectoryProperty outputDir = objects.directoryProperty()

    static TaskProvider<BlogTask> register(Project project, GrailsWebsiteExtension siteExt) {
        project.tasks.register(NAME, BlogTask) {
            it.about.set(siteExt.description)
            it.assetsDir.set(siteExt.assetsDir)
            it.document.set(siteExt.template)
            it.keywords.set(siteExt.keywords)
            it.outputDir.set(siteExt.outputDir)
            it.postsDir.set(siteExt.postsDir)
            it.releases.set(siteExt.releases)
            it.robots.set(siteExt.robots)
            it.title.set(siteExt.title)
            it.url.set(siteExt.url)
        }
    }

    @TaskAction
    void renderBlog() {
        def releasesDir = releases.get().asFile
        def today = new Date()
        def versionsOptions = SiteMap.olderVersions(releasesDir)
                .reverse()
                .collect { "<option>$it</option>" }
                .join(' ')
        def meta = RenderSiteTask.siteMeta(
                title.get(),
                about.get(),
                url.get(),
                keywords.get(),
                robots.get(),
                SiteMap.latestVersion(releasesDir).versionText,
                versionsOptions
        )
        def posts = parsePosts(postsDir.get().asFile)
                .findAll { !it.parsedDate.after(today) }
                .sort(false)
        def processed = processPosts(meta, posts)
        def blogOut = outputDir.dir('dist/blog').get().asFile.tap { it.mkdirs() }
        def templateText = document.get().asFile.text

        renderPosts(meta, processed, blogOut, templateText)

        copyImages(assetsDir.dir('bgimages'), outputDir.dir('dist/images'))
        copyImages(postsDir, outputDir.dir('dist/blog'))
    }

    void copyImages(Provider<Directory> srcDir, Provider<Directory> dstDir) {
        fileSystemOperations.copy { CopySpec copy ->
            copy.from(srcDir)
            copy.into(dstDir)
            copy.include(AssetsTask.assetTypes.images)
        }
    }

    static RssItem rssItemWithPage(
            String title,
            Date pubDate,
            String link,
            String guid,
            String html,
            String author = null
    ) {
        def description = html.contains('<span class="date">')
                ? html.substring(html.indexOf('<span class="date">'))
                        .with { it.substring(it.indexOf('</span>') + '</span>'.length()) }
                : html
        def zdt = Instant.ofEpochMilli(pubDate.time)
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of('GMT'))
        def builder = RssItem.builder()
                .title(title)
                .pubDate(zdt)
                .link(link)
                .guid(guid)
                .description(description)
        if (author) {
            builder.author(parseAuthorName(author))
        }
        builder.build()
    }

    static String parseAuthorName(String author) {
        author.split(/\s*\(/, 2)[0].trim()
    }

    @CompileDynamic
    static String renderPostHtml(HtmlPost htmlPost, String templateText, List<HtmlPost> posts) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    h1 {
                        a(href: '[%url]/blog/index.html', 'Grails Blog')
                    }
                }
            }
            div(class: 'content container') {
                div(class: 'light padded blog-post') {
                    mkp.yieldUnescaped(htmlPost.html)
                    h2(class: 'space-above') {
                        span('You might also like ...')
                    }
                    div(class: 'three-columns') {
                        relatedPosts(htmlPost, posts).each { post ->
                            div(class: 'column') {
                                mkp.yieldUnescaped(BlogTask.postCard(post))
                            }
                        }
                    }
                }
            }
        }

        def metadata = htmlPost.metadata.toMap()
        def html = RenderSiteTask.renderHtmlWithTemplateContent(writer.toString(), metadata, templateText)
        html = RenderSiteTask.highlightMenu(html, metadata, htmlPost.path)

        def bodyClass = metadata.body
        bodyClass ? html.replace('<body>', "<body class='${bodyClass}'>") : html
    }

    static List<HtmlPost> relatedPosts(HtmlPost htmlPost, List<HtmlPost> posts) {
        def byPath = new LinkedHashMap<String, HtmlPost>()  // preserves insertion order

        // 1) Prefer posts that share tags (in tag iteration order)
        htmlPost.tags.each { tag ->
            posts.findAll { it.path != htmlPost.path && it.tags.contains(tag) }
                    .each { p ->
                        if (byPath.size() < MAX_RELATED_POSTS) byPath.putIfAbsent(p.path, p)
                    }
        }

        // 2) Fill remaining slots with other posts (original order)
        if (byPath.size() < MAX_RELATED_POSTS) {
            posts.each { post ->
                if (post.path != htmlPost.path && byPath.size() < MAX_RELATED_POSTS) {
                    byPath.putIfAbsent(post.path, post)
                }
            }
        }

        // 3) Return newest first (and cap)
        byPath.values()
                .toList()
                .sort { DateUtils.parseDate(it.metadata.date) }
                .reverse()
                .take(MAX_RELATED_POSTS)
    }

    static List<HtmlPost> processPosts(Map<String, String> globalMetadata, List<MarkdownPost> markdownPosts) {
        markdownPosts.collect { post ->
            def metadata = RenderSiteTask.processMetadata(globalMetadata + post.metadata)
            def postMetadata = new PostMetadataAdapter(metadata)
            def markdown = post.content
            if (metadata.slides) markdown += "\n\n[Slides](${metadata.slides})\n\n"
            if (metadata.code)   markdown += "\n\n[Code](${metadata.code})\n\n"
            def html = MarkdownUtils.htmlFromMarkdown(markdown)
            RenderSiteTask.parseVideoIframe(metadata)?.with { iframe ->
                html += iframe
            }
            def contentHtml = wrapTags(metadata, html)
            new HtmlPost(
                    metadata: postMetadata,
                    html: contentHtml,
                    path: post.path,
                    tags: parseTags(contentHtml)
            )
        }
    }

    static void renderPosts(
            Map<String, String> globalMetadata,
            List<HtmlPost> listOfPosts,
            File outputDir,
            String templateText
    ) {
        List<String> postCards = []
        List<RssItem> rssItems  = []
        Map<String, Integer> tagsCount = [:].withDefault { 0 }
        Map<String, List<String>> tagPosts = [:].withDefault { [] }

        listOfPosts.each { post ->
            postCards << postCard(post)
            def html = renderPostHtml(post, templateText, listOfPosts)
            new File(outputDir, post.path).tap {
                it.parentFile?.mkdirs()
                it.setText(html, 'UTF-8')
            }
            parseTags(html).each { tag ->
                tagsCount[tag] = tagsCount[tag] + 1
                tagPosts[tag] << post.path
            }
            def link = postLink(post)
            rssItems << rssItemWithPage(
                    post.metadata.title,
                    DateUtils.parseDate(post.metadata.date),
                    link,
                    post.path.replace('.html', ''),
                    post.html,
                    post.metadata.author
            )
        }
        renderArchive(new File(outputDir, INDEX), postCards, globalMetadata, templateText)
        renderRss(globalMetadata, rssItems, new File(outputDir.parentFile, RSS_FILE))
        renderTags(globalMetadata, outputDir, tagsCount.keySet(), listOfPosts, templateText)
    }

    static Set<String> parseTags(String html) {
        def tags = [] as Set<String>
        def m = (html =~ HASHTAG_SPAN)
        while (m.find()) {
            tags.add(m.group(1))
        }
        tags
    }

    static void renderTags(
            Map<String, String> metadata,
            File outputDir,
            Set<String> tags,
            List<HtmlPost> posts,
            String templateText
    ) {
        def tagFolder = new File(outputDir, TAG).tap { mkdirs() }
        def meta = RenderSiteTask.processMetadata(metadata)
        tags.each { tag ->
            def tagCards = posts
                    .findAll { it.tags.contains(tag) }
                    .collect { postCard(it) }
            renderCards(
                    new File(tagFolder, "${tag}.html"),
                    tagCards,
                    new LinkedHashMap(meta).tap {
                        it.title = tag.toUpperCase() + ' | Blog | Grails Framework'
                    },
                    templateText,
                    renderTagTitle(tag)
            )
        }
    }

    static String postLink(HtmlPost post) {
        "$post.metadata.url/$BLOG/$post.path"
    }

    @CompileDynamic
    static String postCard(HtmlPost htmlPost) {
        def meta = htmlPost.metadata
        def imageUrl = meta['image'] ? "$meta.url/$IMAGES/${meta['image']}" : null
        def style = imageUrl ? "background-image: url('$imageUrl')" : null
        def title = RenderSiteTask.replaceLineWithMetadata(meta.title, meta.toMap())
        title = title.size() > MAX_TITLE_LENGTH ? "${title.take(MAX_TITLE_LENGTH)}..." : title
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            omitNullAttributes = true
            article(class: 'blog-card', style: style) {
                a(href: postLink(htmlPost)) {
                    h3 { mkp.yield(RenderSiteTask.formatDate(meta.date)) }
                    h2 { mkp.yield(title) }
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    private static String renderTagTitle(String tag) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).with {
            h1 {
                span('Tag:')
                b(tag)
            }
        }
        writer.toString()
    }

    @CompileDynamic
    private static void renderArchive(
            File f,
            List<String> postCards,
            Map<String, String> siteMeta,
            String templateText
    ) {
        def meta = RenderSiteTask.processMetadata(siteMeta).tap {
            it.title = 'Blog | Grails Framework'
        }
        def html = cardsHtml(postCards)
        html = RenderSiteTask.renderHtmlWithTemplateContent(html, meta, templateText)
        html = RenderSiteTask.highlightMenu(html, meta, "/$BLOG/$INDEX")
        f.setText(html, 'UTF-8')
    }

    private static void renderCards(
            File f,
            List<String> cards,
            Map<String, String> meta,
            String templateText,
            String title = null
    ) {
        f.setText(
                RenderSiteTask.renderHtmlWithTemplateContent(
                        cardsHtml(cards, title),
                        meta,
                        templateText
                ),
                'UTF-8'
        )
    }

    @CompileDynamic
    static String cardsHtml(List<String> cards, String title = null) {
        def writer = new StringWriter()
        new MarkupBuilder(writer).tap {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    if (title) {
                        mkp.yieldUnescaped(title)
                    } else {
                        h1 { a(href: '[%url]/blog/index.html', 'Grails Blog') }
                    }
                }
            }
            div(class: 'clear content container') {
                div(class: 'light') {
                    div(class: 'padded', style: 'padding-top: 0') {
                        cards.collate(3).each { row ->
                            div(class: 'three-columns') {
                                row.each { card ->
                                    div(class: 'column') { mkp.yieldUnescaped(card) }
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.toString()
    }

    private static void renderRss(
            Map<String, String> sitemeta,
            List<RssItem> rssItems,
            File outputFile
    ) {
        def nowGmt = ZonedDateTime.now(ZoneId.of('GMT'))
        def channel = RssChannel.builder(sitemeta.title, sitemeta.url, sitemeta.description)
                .pubDate(nowGmt)
                .lastBuildDate(nowGmt)
                .docs('https://blogs.law.harvard.edu/tech/rss')
                .generator('Micronaut RSS')
                .managingEditor('private@grails.apache.org')
                .webMaster('private@grails.apache.org')
                .tap { b -> rssItems.each { b.item(it) } }
                .build()
        outputFile.withWriter { writer ->
            new DefaultRssFeedRenderer().render(writer, channel)
        }
    }

    static boolean isTag(String word) {
        ALLOWED_TAG_PREFIXES.any {word.startsWith(it) }
    }

    @Nonnull
    static String wrapTags(Map<String, String> metadata, @Nonnull String html) {
        def base = "$metadata.url/$BLOG/$TAG"
        html.readLines().collect { line ->
            if (!(line.startsWith('<p>') && line.endsWith('</p>'))) return line

            def inner = line.replaceFirst(/^<p>/, '').replaceFirst(/<\/p>$/, '')
            def replaced = inner.split(' ').collect { word ->
                if (!isTag(word)) return word
                def tag = word.tokenize('<')[0] // "#foo" from "#foo</span>" etc
                def slug = tag.replace('#', '')
                """<a href="$base/${slug}.html"><span class="hashtag">$tag</span></a>"""
            }.join(' ')
            "<p>$replaced</p>"
        }.join('\n')
    }

    static List<MarkdownPost> parsePosts(File postsDir) {
        postsDir.listFiles()
                .findAll { it.name ==~ /.*\.(md|markdown)$/ }
                .collect { file ->
                    def cm = RenderSiteTask.parseFile(file)
                    new MarkdownPost(
                            filename: file.name,
                            content: cm.content,
                            metadata: cm.metadata
                    )
                }
    }
}

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
package org.grails.gradle.tasks

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Collectors

import javax.annotation.Nonnull
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

import org.grails.utils.DateUtils
import org.grails.HtmlPost
import org.grails.MarkdownPost
import org.grails.PostMetadata
import org.grails.PostMetadataAdapter
import org.grails.documentation.SiteMap
import org.grails.gradle.GrailsWebsiteExtension
import org.grails.utils.MarkdownUtils
import org.grails.tags.Tag
import org.grails.tags.TagCloud

@CompileStatic
@CacheableTask
class BlogTask extends GrailsWebsiteTask {

    @Internal
    String description =
            'Renders Markdown posts (posts/*.md) into HTML pages (dist/blog/*.html). ' +
            'It generates tag pages. Generates RSS feed. ' +
            'Posts with future dates are not generated.'

    public static final String NAME = 'renderBlog'

    private static final String RSS_FILE = 'rss.xml'
    private static final String IMAGES = 'images'
    private static final String HASHTAG_SPAN = '<span class="hashtag">#'
    private static final String SPAN_CLOSE = "</span>"
    private static final int MAX_RELATED_POSTS = 3
    private static final String BLOG = 'blog'
    private static final String TAG = 'tag'
    private static final String INDEX = 'index.html'
    private static List<String> ALLOWED_TAG_PREFIXES = new ArrayList<>()
    private static final int MAX_TITLE_LENGTH = 45

    static {
        def characters = 'A'..'Z'
        def digits = 0..9
        def l = characters.stream()
                .map({str -> "#${str}".toString()})
                .collect(Collectors.toList())
        l.addAll(characters.stream()
                .map({str -> "#${str.toLowerCase()}".toString()})
                .collect(Collectors.toList()))
        l.addAll(digits.stream()
                .map({digit -> "#${digit}".toString()})
                .collect(Collectors.toList()))
        ALLOWED_TAG_PREFIXES = l
    }

    private final ObjectFactory objects

    @Inject
    BlogTask(ObjectFactory objects) {
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

    static TaskProvider<BlogTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, BlogTask) {
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
        def meta = RenderSiteTask.siteMeta(
                title.get(),
                about.get(),
                url.get(),
                keywords.get(),
                robots.get(),
                SiteMap.latestVersion(releases.get().asFile).versionText,
                SiteMap.olderVersions(releases.get().asFile)
                        .reverse()
                        .collect {"<option>$it</option>" }
                        .join(' ')
        )
        renderPosts(
                meta,
                processPosts(
                        meta,
                        parsePosts(postsDir.get().asFile)
                                .findAll {
                                    !it.parsedDate.after(new Date())
                                }
                                .sort(false)
                ),
                outputDir.dir('dist/blog').get().asFile.tap { it.mkdirs() },
                document.get().asFile.text
        )
        copyBackgroundImages()
        copyBlogImages()
    }

    void copyBlogImages() {
        project.copy { CopySpec copy ->
            copy.from(postsDir)
            copy.into(outputDir.dir('dist/blog'))
            copy.include(CopyAssetsTask.IMAGE_EXTENSIONS)
        }
    }

    void copyBackgroundImages() {
        project.copy { CopySpec copy ->
            copy.from(assetsDir.dir('bgimages'))
            copy.into(outputDir.dir('dist/images'))
            copy.include(CopyAssetsTask.IMAGE_EXTENSIONS)
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
        def htmlWithoutTitleAndDate = html
        if (htmlWithoutTitleAndDate.contains('<span class="date">')) {
            htmlWithoutTitleAndDate = htmlWithoutTitleAndDate
                    .substring(htmlWithoutTitleAndDate.indexOf('<span class="date">'))
            htmlWithoutTitleAndDate = htmlWithoutTitleAndDate
                    .substring(htmlWithoutTitleAndDate.indexOf('</span>') + '</span>'.length())
        }
        def builder = RssItem.builder()
                .title(title)
                .pubDate(
                        ZonedDateTime.of(
                                Instant.ofEpochMilli(pubDate.time)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime(), ZoneId.of('GMT')
                        )
                )
                .link(link)
                .guid(guid)
                .description(htmlWithoutTitleAndDate)
        if (author) {
            builder = builder.author(parseAuthorName(author))
        }
        builder.build()
    }

    static String parseAuthorName(String author) {
        author.contains(' (') ? author.substring(0, author.indexOf(' (')) : author
    }

    @CompileDynamic
    static String renderPostHtml(
            HtmlPost htmlPost,
            String templateText,
            List<HtmlPost> posts
    ) {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)
        mb.div(class: 'headerbar chalicesbg') {
            div(class: 'content') {
                h1 {
                    a(href: '[%url]/blog/index.html','Grails Blog')
                }
            }
        }
        mb.div(class: 'content container') {
            div(class: 'light padded blogpost') {
                mkp.yieldUnescaped(htmlPost.html)
                h2(class: 'space-above') {
                    span('You might also like ...')
                }
                div(class: 'threecolumns') {
                    for (HtmlPost post : relatedPosts(htmlPost, posts)) {
                        div(class: 'column') {
                            mkp.yieldUnescaped(postCard(post))
                        }
                    }
                }
            }
        }
        String html = writer.toString()
        Map<String, String> metadata = htmlPost.metadata.toMap()
        html = RenderSiteTask.renderHtmlWithTemplateContent(html, metadata, templateText)
        html = RenderSiteTask.highlightMenu(html, metadata, htmlPost.path)
        metadata['body'] = metadata['body'] ? metadata['body'] : ''
        if (metadata['body']) {
            html = html.replace("<body>", "<body class='${metadata['body']}'>")
        }
        html
    }

    static List<HtmlPost> relatedPosts(HtmlPost htmlPost, List<HtmlPost> posts) {
        List<HtmlPost> relatedPosts = []
        for (String tag : htmlPost.tags) {
            for (HtmlPost p : posts) {
                if (p.tags.contains(tag) && p.path != htmlPost.path) {
                    List<String> paths = relatedPosts*.path
                    if (paths.contains(p.path)) {
                        continue
                    }
                    relatedPosts.add(p)
                    if (relatedPosts.size() > MAX_RELATED_POSTS) {
                        break
                    }
                }
            }
            if (relatedPosts.size() > MAX_RELATED_POSTS) {
                break
            }
        }
        if (relatedPosts.size() < MAX_RELATED_POSTS) {
            for (def p : posts) {
                def paths = relatedPosts*.path
                paths.add(htmlPost.path)
                if (paths.contains(p.path)) {
                    continue
                }
                relatedPosts.add(p)
                if (relatedPosts.size() > MAX_RELATED_POSTS) {
                    break
                }
            }
        }
        relatedPosts.subList(0, MAX_RELATED_POSTS).sort { a, b ->
            DateUtils.parseDate(a.metadata.date).after(DateUtils.parseDate(b.metadata.date)) ? -1 : 1
        }
    }

    static List<HtmlPost> processPosts(Map<String, String> globalMetadata, List<MarkdownPost> markdownPosts) {
        markdownPosts.collect { MarkdownPost mdPost ->
                Map<String, String> metadata = RenderSiteTask.processMetadata(globalMetadata + mdPost.metadata)
            PostMetadata postMetadata = new PostMetadataAdapter(metadata)
            String markdown = mdPost.content
            if (metadata.containsKey('slides')) {
                markdown = markdown + "\n\n[Slides](${metadata['slides']})\n\n"
            }
            if (metadata.containsKey('code')) {
                markdown = markdown + "\n\n[Code](${metadata['code']})\n\n"
            }
            String html = MarkdownUtils.htmlFromMarkdown(markdown)
            String iframe = RenderSiteTask.parseVideoIframe(metadata)
            if (iframe) {
                html = html + iframe
            }
            String contentHtml = wrapTags(metadata, html)
            Set<String> postTags = parseTags(contentHtml)
            new HtmlPost(metadata: postMetadata, html: contentHtml, path: mdPost.path, tags: postTags)
        }
    }

    static void renderPosts(
            Map<String, String> globalMetadata,
            List<HtmlPost> listOfPosts,
            File outputDir,
            final String templateText
    ) {
        List<String> postCards = []
        List<RssItem> rssItems = []
        Map<String, List<String>> tagPosts = [:]
        Map<String, Integer> tagsMap = [:]

        for (HtmlPost htmlPost : listOfPosts) {
            postCards << postCard(htmlPost)
            def html = renderPostHtml(htmlPost, templateText, listOfPosts)
            new File(outputDir, htmlPost.path).tap {
                it.createNewFile()
                it.text = html
            }

            def postTags = parseTags(html)
            for (String postTag : postTags) {
                tagsMap[postTag] = tagsMap.containsKey(postTag) ? (1 + tagsMap[postTag]) : 1
                if (!tagPosts.containsKey(postTag)) {
                    tagPosts[postTag] = []
                }
                tagPosts[postTag] << htmlPost.path
            }
            def postLink = postLink(htmlPost)
            rssItems.add(
                    rssItemWithPage(
                            htmlPost.metadata.title,
                            DateUtils.parseDate(htmlPost.metadata.date),
                            postLink,
                            htmlPost.path.replace('.html', ''),
                            htmlPost.html,
                            htmlPost.metadata.author
                    )
            )
        }
        def tags = tagsMap.collect {k, v ->
            new Tag(title: k, ocurrence: v)
        } as Set<Tag>
        renderArchive(new File(outputDir, 'index.html'), postCards, globalMetadata, templateText, tags)
        renderRss(globalMetadata, rssItems, new File(outputDir.parentFile, RSS_FILE))
        renderTags(globalMetadata, outputDir, tagsMap.keySet(), listOfPosts, templateText)
    }

    static Set<String> parseTags(String html) {
        def pageHtml = html
        def tags = [] as Set<String>
        for (; ;) {
            if (!(pageHtml.contains(HASHTAG_SPAN) && pageHtml.contains(SPAN_CLOSE))) {
                return tags
            }
            pageHtml = pageHtml.substring(pageHtml.indexOf(HASHTAG_SPAN) + HASHTAG_SPAN.length())
            def tag = pageHtml.substring(0, pageHtml.indexOf(SPAN_CLOSE))
            tags << tag
            pageHtml = pageHtml.substring(pageHtml.indexOf(SPAN_CLOSE) + SPAN_CLOSE.length())
        }
    }

    static void renderTags(
            Map<String, String> metadata,
            File outputDir,
            Set<String> tags,
            List<HtmlPost> posts,
            String templateText
    ) {
        def tagFolder = new File(outputDir, TAG).tap {
            it.mkdirs()
        }

        def resolvedMetadata = RenderSiteTask.processMetadata(metadata)

        for (String tag : tags) {
            def tagCards = [] as List<String>
            def postsTagged = posts.findAll { it.tags.contains(tag) }
            for (def post : postsTagged) {
                tagCards << postCard(post)
            }
            def tagFile = new File(tagFolder, "${tag}.html")
            resolvedMetadata['title'] = "${tag.toUpperCase()} | Blog | Grails Framework".toString()
            renderCards(tagFile, tagCards, resolvedMetadata, templateText, renderTagTitle(tag))
        }
    }

    static String postLink(HtmlPost post) {
        "$post.metadata.url/$BLOG/$post.path"
    }

    @CompileDynamic
    private static String postCard(HtmlPost htmlPost) {
        def imageUrl = htmlPost.metadata['image'] ?
                "$htmlPost.metadata.url/$IMAGES/${htmlPost.metadata['image']}".toString() :
                null
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)
        mb.article(class: 'blogcard', style: imageUrl ? 'background-image: url(' + imageUrl + ')' : '') {
            a(href: postLink(htmlPost)) {
                h3 {
                    mkp.yield(
                            RenderSiteTask.formatDate(htmlPost.metadata.date)
                    )
                }
                h2 {
                    def title = RenderSiteTask.replaceLineWithMetadata(
                            htmlPost.metadata.title,
                            htmlPost.metadata.toMap()
                    )
                    if (title.length() > MAX_TITLE_LENGTH) {
                        title = "${title.substring(0, MAX_TITLE_LENGTH)}..."
                    }
                    mkp.yield(title)
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    private static String renderTagTitle(String tag) {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)
        mb.h1 {
            span('Tag:')
            b(tag)
        }
        writer.toString()
    }

    @CompileDynamic
    private static String tagsCard(Map<String, String> sitemeta, Set<Tag> tags) {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)
        mb.article(class: 'tagcloud blogcard desktop') {
            h3('Post by Tag')
            mkp.yieldUnescaped(
                    TagCloud.tagCloud(
                            "${sitemeta['url']}/${BLOG}/${TAG}",
                            tags,
                            false
                    )
            )
        }
        writer.toString()
    }

    @CompileDynamic
    private static String rssCard(String url) {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)
        def imageUrl = "$url/images/feedicon.svg"
        mb.article(
                class: 'blogcard desktop',
                style: /background-image: url('/ + imageUrl + /')/
        ) {
            h3('Feeds')
            h2 {
                a(href: '[%url]/' + RSS_FILE, 'RSS Feed')
            }
        }
        writer.toString()
    }

    @CompileDynamic
    private static String subscribeCard() {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)
        mb.article(class: 'blogcard desktop') {
            mkp.yieldUnescaped('''
<!--[if lte IE 8]>
<script charset="utf-8" type="text/javascript" src="//js.hsforms.net/forms/v2-legacy.js"></script>
<![endif]-->
<script charset="utf-8" type="text/javascript" src="//js.hsforms.net/forms/v2.js"></script>
<script>
  hbspt.forms.create({
\tportalId: "4547412",
\tformId: "a675210e-7748-44bf-b603-3363d613ffb1"
});
</script>
''')
        }
        writer.toString()
    }

    @CompileDynamic
    private static void renderArchive(
            File f,
            List<String> postCards,
            Map<String, String> sitemeta,
            String templateText,
            Set<Tag> tags
    ) {
        def cards = [] as List<String>
        cards.addAll(postCards)
//        cards.add(2, tagsCard(sitemeta, tags))
//        cards.add(5, rssCard(sitemeta['url']))
        //cards.add(8, subscribeCard())
        def resolvedMetadata = RenderSiteTask.processMetadata(sitemeta)
        // String html = EventsPage.mainContent(sitemeta['url']) +
        //         cardsHtml(cards, resolvedMetadata)
        def html = cardsHtml(cards, resolvedMetadata)
        resolvedMetadata['title'] = 'Blog | Grails Framework'
        html = RenderSiteTask.renderHtmlWithTemplateContent(html, resolvedMetadata, templateText)
        html = RenderSiteTask.highlightMenu(html, resolvedMetadata, "/$BLOG/$INDEX")
        f.createNewFile()
        f.text = html
    }

    private static void renderCards(
            File f,
            List<String> cards,
            Map<String, String> meta,
            String templateText,
            String title = null
    ) {
        def pageHtml = cardsHtml(cards, meta, title)
        f.createNewFile()
        f.text = RenderSiteTask.renderHtmlWithTemplateContent(pageHtml, meta, templateText)
    }

    @CompileDynamic
    static String cardsHtml(
            List<String> cards,
            Map<String, String> meta,
            String title = null
    ) {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)
        mb.div(class: 'headerbar chalicesbg') {
            div(class: 'content') {
                if (title) {
                    mkp.yieldUnescaped(title)
                } else {
                    h1 {
                        a(href: '[%url]/blog/index.html', 'Grails Blog')
                    }
                }
            }
        }
        mb.div(class: 'clear content container') {
//            if (title) {
//                mkp.yieldUnescaped(title)
//            } else {
//
//            }

            div(class: 'light') {
                div(class: 'padded', style: 'padding-top: 0;') {
                    for (int i = 0; i < cards.size(); i++) {
                        if (i == 0) {
                            mkp.yieldUnescaped('<div class="threecolumns">')
                        }
                        div(class: 'column') {
                            mkp.yieldUnescaped(cards[i])
                        }
                        if (i != 0 && ((i + 1 ) % 3 == 0)) {
                            mkp.yieldUnescaped('</div>')
                            if (i != (cards.size() - 1)) {
                                mkp.yieldUnescaped('<div class="threecolumns">')
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
        def builder = RssChannel.builder(
                sitemeta['title'],
                sitemeta['url'],
                sitemeta['description']
        )
        builder.pubDate(
                ZonedDateTime.of(
                        LocalDateTime.now(),
                        ZoneId.of('GMT')
                )
        )
        builder.lastBuildDate(
                ZonedDateTime.of(
                        LocalDateTime.now(),
                        ZoneId.of('GMT')
                )
        )
        .docs('https://blogs.law.harvard.edu/tech/rss')
        .generator('Micronaut RSS')
        .managingEditor('delamos@objectcomputing.com')
        .webMaster('delamos@objectcomputing.com')

        for (RssItem item : rssItems) {
            builder.item(item)
        }
        def writer = new FileWriter(outputFile)
        new DefaultRssFeedRenderer().with {
            render(writer, builder.build())
        }
        writer.close()
    }

    static boolean isTag(String word) {
        ALLOWED_TAG_PREFIXES.any {word.startsWith(it) }
    }

    @Nonnull
    static String wrapTags(Map<String, String> metadata, @Nonnull String html) {
        html.split('\n')
            .collect { line ->
                if (line.startsWith('<p>') && line.endsWith('</p>')) {
                    def lineWithoutParagraphs = line
                            .replaceAll('<p>', '')
                            .replaceAll('</p>', '')
                    def words = lineWithoutParagraphs.split(' ')
                    lineWithoutParagraphs = words.collect { word ->
                        if (isTag(word)) {
                            def tag = word
                            if (word.contains('<')) {
                                tag = word.substring(0, word.indexOf('<'))
                            }
                            return "<a href=\"${metadata['url']}/$BLOG/$TAG/${tag.replaceAll('#', '')}.html\"><span class=\"hashtag\">$tag</span></a>".toString()
                        } else {
                            return word
                        }
                    }.join(' ')
                    return "<p>$lineWithoutParagraphs</p>".toString()
                } else {
                    return line
                }
            }.join('\n')
    }

    static List<MarkdownPost> parsePosts(File postsDir) {
        List<MarkdownPost> listOfPosts = []
        postsDir.eachFile { file ->
            if (file.path.endsWith('.md') || file.path.endsWith('.markdown')) {
                def contentAndMetadata = RenderSiteTask.parseFile(file)
                listOfPosts.add(
                        new MarkdownPost(
                                filename: file.name,
                                content: contentAndMetadata.content,
                                metadata: contentAndMetadata.metadata
                        )
                )
            }
        }
        listOfPosts
    }
}

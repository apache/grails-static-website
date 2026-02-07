package org.grails.gradle


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import org.grails.ContentAndMetadata
import org.grails.HtmlPost
import org.grails.MarkdownPost
import org.grails.documentation.SiteMap
import org.grails.documentation.SoftwareVersion
import org.grails.markdown.MarkdownUtil
import org.grails.PostMetadata
import org.grails.PostMetadataAdapter
import io.micronaut.rss.DefaultRssFeedRenderer
import io.micronaut.rss.RssChannel
import io.micronaut.rss.RssFeedRenderer
import io.micronaut.rss.RssItem
import org.grails.tags.Tag
import org.grails.tags.TagCloud
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.CopySpec
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.annotation.Nonnull
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Collectors

@CompileStatic
class BlogTask extends DefaultTask {
    static final SimpleDateFormat MMM_D_YYYY_HHMM = new SimpleDateFormat("MMM d, yyyy HH:mm")
    static final SimpleDateFormat MMM_D_YYYY = new SimpleDateFormat("MMM d, yyyy")
    static final SimpleDateFormat MMMM_D_YYYY = new SimpleDateFormat("MMMM d, yyyy")
    public static final String RSS_FILE = 'rss.xml'
    public static final String IMAGES = 'images'
    final static String HASHTAG_SPAN = "<span class=\"hashtag\">#"
    final static String SPAN_CLOSE = "</span>"
    public static final int MAX_RELATED_POSTS = 3
    public static final String BLOG = 'blog'
    public static final String TAG = 'tag'
    public static final String INDEX = 'index.html'

    public static List<String> ALLOWED_TAG_PREFIXES = new ArrayList<>()
    public static final int MAX_TITLE_LENGTH = 45
    static {
        List<String> characters = 'A'..'Z'
        List<Integer> digits = [0,1,2,3,4,5,6,7,8,9]
        List<String> l = characters.stream()
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

    @Input
    final Property<File> document = project.objects.property(File)

    @Input
    final Property<String> title = project.objects.property(String)

    @Input
    final Property<String> about = project.objects.property(String)

    @Input
    final Property<String> url = project.objects.property(String)

    @Input
    final ListProperty<String> keywords = project.objects.listProperty(String)

    @Input
    final Property<String> robots = project.objects.property(String)

    @InputDirectory
    final Property<File> posts = project.objects.property(File)

    @OutputDirectory
    final Property<File> output = project.objects.property(File)

    @InputDirectory
    final Property<File> assets = project.objects.property(File)

    File dist() {
        new File(output.get().absolutePath + "/" + RenderSiteTask.DIST)
    }

    @Input
    final Property<File> releases = project.objects.property(File)

    @TaskAction
    void renderBlog() {
        File template = document.get()
        final String templateText = template.text
        File o = dist()

        File releasesFile = releases.get()
        SoftwareVersion latest = SiteMap.latestVersion(releasesFile)
        List<String> olderVersions = SiteMap.olderVersions(releasesFile).reverse()
        String versions = olderVersions.collect {version -> "<option>${version}</option>" }.join(' ')

        Map<String, String> m = RenderSiteTask.siteMeta(title.get(),
                about.get(),
                url.get(),
                keywords.get() as List<String>,
                robots.get(),
                latest.versionText,
                versions
        )
        copyBackgroundImages()
        List<MarkdownPost> listOfPosts = parsePosts(posts.get())
        listOfPosts = filterOutFuturePosts(listOfPosts)
        listOfPosts = listOfPosts.sort { a, b ->
            parseDate(a.date).after(parseDate(b.date)) ? -1 : 1
        }
        List<HtmlPost> htmlPosts = processPosts(m, listOfPosts)
        File blog = new File(o.absolutePath + '/' + BLOG)
        blog.mkdir()
        renderPosts(m, htmlPosts, blog, templateText)
        copyBlogImages()
    }

    static List<MarkdownPost> filterOutFuturePosts(List<MarkdownPost> posts) {
        posts.findAll { post -> !parseDate(post.date).after(new Date()) }
    }

    static Date parseDate(String date) throws ParseException {

        try {
            return MMM_D_YYYY_HHMM.parse(date)
        } catch(ParseException e) {
            try {
                return MMM_D_YYYY.parse(date)
            } catch(ParseException ex) {
                throw new GradleException("Could not parse date $date")
            }
        }
    }

    void copyBlogImages() {
        File images = new File(posts.get().absolutePath)
        File outputBlogImages = new File(dist().absolutePath + '/' + BLOG)
        outputBlogImages.mkdir()
        project.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec copySpec) {
                copySpec.from(images)
                copySpec.into(outputBlogImages)
                copySpec.include(CopyAssetsTask.IMAGE_EXTENSIONS)
            }
        })
    }

    void copyBackgroundImages() {
        File images = new File(assets.get().absolutePath + "/" + "bgimages")
        File outputImages = new File(dist().absolutePath + '/images')
        outputImages.mkdir()
        project.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec copySpec) {
                copySpec.from(images)
                copySpec.into(outputImages)
                copySpec.include(CopyAssetsTask.IMAGE_EXTENSIONS)
            }
        })
    }

    static RssItem rssItemWithPage(String title,
                                   Date pubDate,
                                   String link,
                                   String guid,
                                   String html,
                                   String author) {
        String htmlWithoutTitleAndDate = html
        if (htmlWithoutTitleAndDate.contains('<span class="date">')) {
            htmlWithoutTitleAndDate = htmlWithoutTitleAndDate.substring(htmlWithoutTitleAndDate.indexOf('<span class="date">'))
            htmlWithoutTitleAndDate = htmlWithoutTitleAndDate.substring(htmlWithoutTitleAndDate.indexOf('</span>') + '</span>'.length())
        }
        RssItem.Builder builder = RssItem.builder()
                .title(title)
                .pubDate(ZonedDateTime.of(Instant.ofEpochMilli(pubDate.time)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(), ZoneId.of("GMT")))
                .link(link)
                .guid(guid)
                .description(htmlWithoutTitleAndDate)
        if (author) {
            builder = builder.author(parseAuthorName(author))
        }
        builder.build()
    }

    static String parseAuthorName(String author) {
        author.contains(" (") ? author.substring(0, author.indexOf(' (')) : author
    }

    @CompileDynamic
    static String renderPostHtml(HtmlPost htmlPost,
                                 String templateText,
                                 List<HtmlPost> posts) {

        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)
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
                h2 (class: 'space-above') {
                    span "You might also like ..."
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
        metadata['ogurl'] = postLink(htmlPost)
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
            for (HtmlPost p : posts) {
                List<String> paths = relatedPosts*.path
                paths.add(htmlPost.path)
                if (paths.contains(p.path)) {
                    continue
                }
                relatedPosts << p
                if (relatedPosts.size() > MAX_RELATED_POSTS) {
                    break
                }
            }
        }
        relatedPosts.subList(0, MAX_RELATED_POSTS).sort { a, b ->
            parseDate(a.metadata.date).after(parseDate(b.metadata.date)) ? -1 : 1
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
            String html = MarkdownUtil.htmlFromMarkdown(markdown)
            String iframe = RenderSiteTask.parseVideoIframe(metadata)
            if (iframe) {
                html = html + iframe
            }
            String contentHtml = wrapTags(metadata, html)
            Set<String> postTags = parseTags(contentHtml)
            new HtmlPost(metadata: postMetadata, html: contentHtml, path: mdPost.path, tags: postTags)
        }
    }

    static void renderPosts(Map<String, String> globalMetadata,
                            List<HtmlPost> listOfPosts,
                            File outputDir,
                            final String templateText) {
        List<String> postCards = []
        List<RssItem> rssItems = []
        Map<String, List<String>> tagPosts = [:]
        Map<String, Integer> tagsMap = [:]

        for (HtmlPost htmlPost : listOfPosts) {
            postCards << postCard(htmlPost)
            String html = renderPostHtml(htmlPost, templateText, listOfPosts)
            File pageOutput = new File(outputDir.absolutePath + "/" + htmlPost.path)
            pageOutput.createNewFile()
            pageOutput.text = html

            Set<String> postTags = parseTags(html)
            for (String postTag : postTags) {
                tagsMap[postTag] = tagsMap.containsKey(postTag) ? (1 + tagsMap[postTag]) : 1
                if (!tagPosts.containsKey(postTag)) {
                    tagPosts[postTag] = []
                }
                tagPosts[postTag] << htmlPost.path
            }
            String postLink = postLink(htmlPost)
            rssItems.add(rssItemWithPage(htmlPost.metadata.title,
                    parseDate(htmlPost.metadata.date),
                    postLink,
                    htmlPost.path.replace(".html", ""),
                    htmlPost.html,
                    htmlPost.metadata.author))
        }
        Set<Tag> tags = tagsMap.collect { k, v -> new Tag(title: k, ocurrence: v) } as Set<Tag>
        renderArchive(new File(outputDir.absolutePath + "/index.html"), postCards, globalMetadata, templateText, tags)
        renderRss(globalMetadata, rssItems, new File(outputDir.absolutePath + "/../" + RSS_FILE))
        renderTags(globalMetadata, outputDir, tagsMap.keySet(), listOfPosts, templateText)
    }

    static Set<String> parseTags(String html) {
        String pageHtml = html
        Set<String> tags = []
        for (; ;) {
            if (!(pageHtml.contains(HASHTAG_SPAN) && pageHtml.contains(SPAN_CLOSE))) {
                return tags
            }
            pageHtml = pageHtml.substring(pageHtml.indexOf(HASHTAG_SPAN) + HASHTAG_SPAN.length())
            String tag = pageHtml.substring(0, pageHtml.indexOf(SPAN_CLOSE))
            tags << tag
            pageHtml = pageHtml.substring(pageHtml.indexOf(SPAN_CLOSE) + SPAN_CLOSE.length())
        }
    }

    static void renderTags(Map<String, String> metadata,
                           File outputDir,
                           Set<String> tags,
                           List<HtmlPost> posts,
                           String templateText) {
        File tagFolder = new File(outputDir.absolutePath + "/${TAG}")
        tagFolder.mkdir()

        Map<String, String> resolvedMetadata = RenderSiteTask.processMetadata(metadata)

        for (String tag : tags) {
            List<String> tagCards = []
            List<HtmlPost> postsTagged = posts.findAll { it.tags.contains(tag) }
            for (HtmlPost post : postsTagged) {
                tagCards << postCard(post)
            }
            File tagFile = new File("${tagFolder.absolutePath}/${tag}.html")
            resolvedMetadata['title'] = "${tag.toUpperCase()} | Blog | Grails Framework".toString()
            renderCards(tagFile, tagCards, resolvedMetadata, templateText, renderTagTitle(tag))
        }
    }

    static String postLink(HtmlPost post) {
        post.metadata.url + '/' + BLOG + '/' + post.path
    }

    @CompileDynamic
    private static String postCard(HtmlPost htmlPost) {
        String imageUrl = htmlPost.metadata['image'] ? (htmlPost.metadata.url + '/' + IMAGES + '/' + htmlPost.metadata['image']) : null
        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)
        mb.article(class: 'blogcard', style: imageUrl ? 'background-image: url(' + imageUrl + ')' : '') {
            a(href: postLink(htmlPost)) {
                h3 {
                    mkp.yield RenderSiteTask.formatDate(htmlPost.metadata.date)
                }
                h2 {
                    String title = RenderSiteTask.replaceLineWithMetadata(htmlPost.metadata.title, htmlPost.metadata.toMap())
                    if (title.length() > MAX_TITLE_LENGTH) {
                        title ="${title.substring(0, MAX_TITLE_LENGTH)}..."
                    }
                    mkp.yield title
                }
            }
        }
        writer.toString()
    }

    @CompileDynamic
    private static String renderTagTitle(String tag) {
        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)
        mb.h1 {
            span 'Tag:'
            b tag
        }
        writer.toString()
    }

    @CompileDynamic
    private static String tagsCard(Map<String, String> sitemeta, Set<Tag> tags) {
        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)
        mb.article(class: 'tagcloud blogcard desktop') {
            h3 'Post by Tag'
            mkp.yieldUnescaped TagCloud.tagCloud(sitemeta['url'] + "/" + BLOG + "/" + TAG , tags, false)
        }
        return writer.toString()
    }

    @CompileDynamic
    private static String rssCard(String url) {
        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)
        String imageUrl = url + "/images/feedicon.svg"
        mb.article(class: 'blogcard desktop', style: "background-image: url('${imageUrl}')".toString()) {
            h3 'Feeds'
            h2 {
                a(href: '[%url]/' + RSS_FILE, 'RSS Feed')
            }
        }
        return writer.toString()
    }
    @CompileDynamic
    private static String subscribeCard() {
        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)
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
    private static void renderArchive(File f,
                                      List<String> postCards,
                                      Map<String, String> sitemeta,
                                      String templateText,
                                      Set<Tag> tags) {

        List<String> cards = []
        cards.addAll(postCards)
//        cards.add(2, tagsCard(sitemeta, tags))
//        cards.add(5, rssCard(sitemeta['url']))
        //cards.add(8, subscribeCard())
        Map<String, String> resolvedMetadata = RenderSiteTask.processMetadata(sitemeta)
        // String html = EventsPage.mainContent(sitemeta['url']) +
        //         cardsHtml(cards, resolvedMetadata)
        String html = cardsHtml(cards, resolvedMetadata)
        resolvedMetadata['title'] = 'Blog | Grails Framework'
        html = RenderSiteTask.renderHtmlWithTemplateContent(html, resolvedMetadata, templateText)

        html = RenderSiteTask.highlightMenu(html, resolvedMetadata, "/" + BLOG + "/" + INDEX)
        f.createNewFile()
        f.text = html
    }

    private static void renderCards(File f,
                                    List<String> cards,
                                    Map<String, String> meta,
                                    String templateText,
                                    String title = null) {
        String pageHtml = cardsHtml(cards, meta, title)
        f.createNewFile()
        f.text = RenderSiteTask.renderHtmlWithTemplateContent(pageHtml, meta, templateText)
    }

    @CompileDynamic
    static String cardsHtml(List<String> cards, Map<String, String> meta, String title = null) {
        StringWriter writer = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writer)

        mb.div(class: 'headerbar chalicesbg') {
            div(class: 'content') {
                if (title) {
                    mkp.yieldUnescaped(title)
                } else {
                    h1 {
                        a(href: '[%url]/blog/index.html','Grails Blog')
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

                        if ( i != 0 && ((i + 1 ) % 3 == 0)) {
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

    private static void renderRss(Map<String, String> sitemeta, List<RssItem> rssItems, File outputFile) {
        RssChannel.Builder builder = RssChannel.builder(sitemeta['title'], sitemeta['url'], sitemeta['description'])
        builder.pubDate(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("GMT")))
        builder.lastBuildDate(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("GMT")))
                .docs("https://blogs.law.harvard.edu/tech/rss")
                .generator("Micronaut RSS")
                .managingEditor("delamos@objectcomputing.com")
                .webMaster("delamos@objectcomputing.com")
        for (RssItem item : rssItems) {
            builder.item(item)
        }
        FileWriter writer = new FileWriter(outputFile)
        RssFeedRenderer rssFeedRenderer = new DefaultRssFeedRenderer()
        rssFeedRenderer.render(writer, builder.build())
        writer.close()
    }

    static boolean isTag(String word) {
        ALLOWED_TAG_PREFIXES.any {word.startsWith(it) }
    }

    @Nonnull
    static String wrapTags(Map<String, String> metadata, @Nonnull String html) {
        html.split("\n")
                .collect { line ->
                    if (line.startsWith("<p>") && line.endsWith("</p>")) {
                        String lineWithoutParagraphs = line.replaceAll("<p>", "")
                                .replaceAll("</p>", "")
                        String[] words = lineWithoutParagraphs.split(" ")
                        lineWithoutParagraphs = words.collect { word ->
                            if (isTag(word)) {
                                String tag = word
                                if (word.contains("<")) {
                                    tag = word.substring(0, word.indexOf("<"))
                                }
                                return "<a href=\"${metadata['url']}/${BLOG}/${TAG}/${tag.replaceAll("#", "")}.html\"><span class=\"hashtag\">${tag}</span></a>".toString()
                            } else {
                                return word
                            }
                        }.join(" ")
                        return "<p>${lineWithoutParagraphs}</p>".toString()
                    } else {
                        line
                    }
                }.join('\n')
    }

    static List<MarkdownPost> parsePosts(File posts) {
        List<MarkdownPost> listOfPosts = []
        posts.eachFile { file ->
            if (file.path.endsWith(".md") || file.path.endsWith(".markdown")) {
                ContentAndMetadata contentAndMetadata = RenderSiteTask.parseFile(file)
                listOfPosts << new MarkdownPost(filename: file.name, content: contentAndMetadata.content, metadata: contentAndMetadata.metadata)
            }
        }
        listOfPosts
    }


}

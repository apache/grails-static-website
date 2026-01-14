package org.grails.gradle

import edu.umd.cs.findbugs.annotations.Nullable
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.ContentAndMetadata
import org.grails.Page
import io.micronaut.context.ApplicationContext
import org.grails.documentation.SiteMap
import org.grails.documentation.SoftwareVersion
import org.grails.events.Event
import org.grails.events.EventsPage

import io.micronaut.inject.qualifiers.Qualifiers
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import static groovy.io.FileType.FILES
import groovy.time.TimeCategory

import javax.annotation.Nonnull
import javax.validation.constraints.NotNull

@CompileStatic
class RenderSiteTask extends DefaultTask {

    public static final String YOUTUBE_WATCH = 'https://www.youtube.com/watch?v='

    static final String COLON = ":"
    static final String SEPARATOR = "---"
    public static final String DIST = "dist"
    public static final int TWITTER_CARD_PLAYER_WIDTH = 560
    public static final int TWITTER_CARD_PLAYER_HEIGHT = 315

    @InputDirectory
    final Property<File> pages = project.objects.property(File)

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

    @OutputDirectory
    final Property<File> output = project.objects.property(File)

    @Input
    final Property<File> releases = project.objects.property(File)

    @TaskAction
    void renderSite() {
        File template = document.get()
        final String templateText = template.text
        File o = output.get()
        File releasesFile = releases.get()
        SoftwareVersion latest = SiteMap.latestVersion(releasesFile)
        String versions = SiteMap.olderVersions(releasesFile).reverse().collect {version -> "<option>${version}</option>" }.join(' ')
        Map<String, String> m = siteMeta(title.get(),
                about.get(),
                url.get(),
                keywords.get() as List<String>,
                robots.get(),
                latest.versionText,
                versions)
        List<Page> listOfPages = parsePages(pages.get())
        listOfPages.addAll(parsePages(new File(o.absolutePath + "/" + DocumentationTask.TEMP)))
        File dist = new File(o.absolutePath + "/" + DIST)
        renderPages(m, listOfPages, dist, templateText)
    }

    static Map<String, String> siteMeta(String title,
                                        String about,
                                        String url,
                                        List<String> keywords,
                                        String robots,
                                        String latest,
                                        String versionsBeforeGrails6
    ) {
        String eventsHtml = ""
        [
                title: title,
                description: about,
                url: url,
                latest: latest,
                events: eventsHtml,
                versions: versionsBeforeGrails6,
                versionAfterGrails6: versionsBeforeGrails6,
                keywords: keywords.join(','),
                robots: robots,
        ] as Map<String, String>
    }

    static void renderPages(Map<String, String> sitemeta, List<Page> listOfPages, File outputDir, final String templateText) {
        for (Page page : listOfPages) {
            Map<String, String> resolvedMetadata = processMetadata(sitemeta + page.metadata)
            String html = renderHtmlWithTemplateContent(page.content, resolvedMetadata, templateText)
            html = highlightMenu(html, sitemeta, page.path)
            if (page.body) {
                html = html.replace("<body>", "<body class='${page.body}'>")
            }
            saveHtmlToPath(outputDir, html, page.path)
        }
    }

    static void saveHtmlToPath(File outputDir, String html, String filepath) {
        File pageOutput = new File(outputDir.absolutePath)
        pageOutput.mkdir()
        String[] paths = filepath.split('/')
        for (String path : paths) {
            if (path.endsWith(".html")) {
                pageOutput = new File(pageOutput.getAbsolutePath() + "/" + path)
            } else if (path.trim().isEmpty()) {
                continue
            } else {
                pageOutput = new File(pageOutput.getAbsolutePath() + "/" + path)
                pageOutput.mkdir()
            }
        }
        pageOutput.createNewFile()
        pageOutput.text = html
    }

    static Map<String, String> processMetadata(Map<String, String> sitemeta) {
        Map<String, String> resolvedMetadata = sitemeta
        if (resolvedMetadata.containsKey("CSS")) {
        
            resolvedMetadata.put("CSS", "<link rel='stylesheet' href='[%url]" + resolvedMetadata['CSS'] + "'/>")
        } else {
            resolvedMetadata.put("CSS", "")
        }

        if (resolvedMetadata.containsKey("JAVASCRIPT")) {
            resolvedMetadata.put("JAVASCRIPT", "<script src='" + resolvedMetadata['JAVASCRIPT'] + "'></script>")
        } else {
            resolvedMetadata.put("JAVASCRIPT", "")
        }

        if (!resolvedMetadata.containsKey("HTML header")) {
            resolvedMetadata.put("HTML header", "")
        }
        if (!resolvedMetadata.containsKey("keywords")) {
            resolvedMetadata.put('keywords', "")
        }
        if (!resolvedMetadata.containsKey("description")) {
            resolvedMetadata.put('description', "")
        }
        if (!resolvedMetadata.containsKey("date")) {
            resolvedMetadata.put('date', BlogTask.MMM_D_YYYY_HHMM.format(new Date()))
        }
        if (!resolvedMetadata.containsKey("robots")) {
            resolvedMetadata.put('robots', "all")
        }
        resolvedMetadata.put('twittercard', twitterCard('summary_large_image'))
        if (resolvedMetadata.containsKey('video')) {
            String videoId = parseVideoId(resolvedMetadata)
            if (videoId) {
                resolvedMetadata.put('twittercard', twitterCard('player') + twitterPlayerHtml(videoId, TWITTER_CARD_PLAYER_WIDTH, TWITTER_CARD_PLAYER_HEIGHT))
            }
        }
        if (resolvedMetadata.containsKey('video') && parseVideoId(resolvedMetadata)) {

        } else {

        }

        resolvedMetadata
    }

    @Nullable
    static String parseVideoId(Map<String, String> metadata) {
        metadata.containsKey('video') && metadata['video'].startsWith(YOUTUBE_WATCH) ? metadata['video'].substring(YOUTUBE_WATCH.length()) : null
    }

    @Nullable
    static String parseVideoIframe(Map<String, String> metadata) {
        String videoId = parseVideoId(metadata)
        videoId ? "<iframe width=\"100%\" height=\"560\" src=\"https://www.youtube-nocookie.com/embed/"+videoId+"\" frameborder=\"0\"></iframe>" : null
    }

    static String twitterPlayerHtml(String videoId, int width, int height) {
"""\
<meta name='twitter:player' content='https://www.youtube.com/embed/${videoId}' />
<meta name='twitter:player:width' content='${width}' />
<meta name='twitter:player:height' content='${height}' />
"""
    }

    static String twitterCard(String cardType) {
        "<meta name='twitter:card' content='${cardType}'/>"
    }

    static String highlightMenu(String html, Map<String, String> sitemeta, String path) {
        html.replaceAll("<li><a href='" + sitemeta['url'] + path, "<li class='active'><a href='" + sitemeta['url'] + path)
    }

    static List<Page> parsePages(File pages) {
        List<Page> listOfPages = []
        pages.eachFileRecurse(FILES) { file ->
            if (file.path.endsWith(".html")) {
                ContentAndMetadata contentAndMetadata = parseFile(file)
                String filename = file.absolutePath.replace(pages.absolutePath, "")
                listOfPages << new Page(filename: filename, content: contentAndMetadata.content, metadata: contentAndMetadata.metadata)
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

        !metadataProcessed || lines.isEmpty() ? new ContentAndMetadata(metadata: [:] as Map<String, String>, content: file.text) :
                new ContentAndMetadata(metadata: metadata, content: lines.join("\n"))
    }

    @Nonnull
    static String renderHtmlWithTemplateContent(@Nonnull @NotNull String html,
                                         @Nonnull @NotNull Map<String, String> meta,
                                         @NotNull @Nonnull final String templateText) {
        String outputHtml = templateText
        String result = outputHtml.replace(' data-document>', ">" + html)
        result = replaceLineWithMetadata(result, meta)
        result
    }

    static String formatDate(String date) {
        BlogTask.MMMM_D_YYYY.format(BlogTask.parseDate(date))
    }

    @CompileDynamic
    static String formatDateMinus6Months(String date) {
        use(TimeCategory) {
            BlogTask.MMMM_D_YYYY.format(BlogTask.parseDate(date) - 6.months)
        }
    }

    static String URLEncode(String date) {
        URLEncoder.encode(date, "UTF-8")
    }

    static String replaceLineWithMetadata(String line, Map<String, String> metadata) {
        Map<String, String> m = new HashMap<>(metadata)
        if (m.containsKey('date')) {
            m['date'] = formatDate(m['date'])
            m['6MonthsBackForGitHub'] = '?from=' + URLEncode(formatDateMinus6Months(m['date'])) + '&to=' + URLEncode(formatDate(m['date']))
        }
        for (String metadataKey : m.keySet()) {
            if (line.contains("[%${metadataKey}]".toString())) {
                String value = m[metadataKey]
                if ("[%${metadataKey}]".toString() == '[%author]') {
                    List<String> authors = value.split(",") as List<String>
                    value = '<span class="author">By ' + authors.join("<br/>") + '</span>'
                    line = line.replaceAll("\\[%${metadataKey}\\]".toString(), value)

                } else if ("[%${metadataKey}]".toString() == '[%date]') {
                    if (line.contains('<meta')) {
                        line = line.replaceAll("\\[%${metadataKey}\\]".toString(), value)
                    } else {
                        value = '<span class="date">' + value + '</span>'
                        line = line.replaceAll("\\[%${metadataKey}\\]".toString(), value)
                    }
                } else {
                    line = line.replaceAll("\\[%${metadataKey}\\]".toString(), value)
                }
            }
        }
        line
    }
}

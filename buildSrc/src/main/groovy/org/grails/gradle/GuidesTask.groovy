package org.grails.gradle

import groovy.transform.CompileStatic
import org.grails.documentation.SiteMap
import org.grails.documentation.SoftwareVersion
import org.grails.guides.Guide
import org.grails.guides.GuidesFetcher
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.grails.ContentAndMetadata
import org.grails.Page
import org.grails.guides.Category
import org.grails.guides.GuidesPage
import org.grails.guides.TagUtils
import org.grails.tags.Tag

@CompileStatic
class GuidesTask extends DefaultTask {

    static final String PAGE_NAME_GUIDES = "guides.html"
    public static final String CATEGORIES = "categories"
    public static final String TAGS = "tags"

    @Input
    final Property<File> document = project.objects.property(File)

    @OutputDirectory
    final Property<File> output = project.objects.property(File)

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

    @Input
    final Property<File> releases = project.objects.property(File)

    @TaskAction
    void renderGuides() {
        File pagesDir = new File(output.get().absolutePath + "/" + DocumentationTask.TEMP)
        pagesDir.mkdir()
        ClassLoader classLoader = this.getClass().getClassLoader()

        generateGuidesPages(classLoader, pagesDir, url.get())
        File template = document.get()
        final String templateText = template.text
        File o = new File(output.get().absolutePath + "/" + RenderSiteTask.DIST + "/guides")
        o.mkdirs()


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
                versions)
        File f = new File(pagesDir.absolutePath + "/" + GuidesTask.PAGE_NAME_GUIDES)
        Page page = pageWithFile(f)
        page.filename = 'index.html'
        RenderSiteTask.renderPages(m, [page], o, templateText)

        List<Page> listOfPages = parseCategoryPages(pagesDir)
        File categoriesOutput = new File(o.absolutePath + "/" + CATEGORIES)
        categoriesOutput.mkdir()
        RenderSiteTask.renderPages(m, listOfPages, categoriesOutput, templateText)

        listOfPages = parseTagsPages(pagesDir)
        File tagOutput = new File(o.absolutePath + "/" + TAGS)
        tagOutput.mkdir()
        RenderSiteTask.renderPages(m, listOfPages, tagOutput, templateText)
    }

    static List<Page> parseCategoryPages(File pages) {
        List<Page> listOfPages = []
        new File(pages.absolutePath + "/" + CATEGORIES).eachFile { categoryFile ->
            listOfPages << pageWithFile(categoryFile)
        }
        listOfPages
    }

    static List<Page> parseTagsPages(File pages) {
        List<Page> listOfPages = []
        new File(pages.absolutePath + "/" + TAGS).eachFile { tagFile ->
            listOfPages << pageWithFile(tagFile)
        }
        listOfPages
    }

    static Page pageWithFile(File f) {
        ContentAndMetadata contentAndMetadata = RenderSiteTask.parseFile(f)
        new Page(filename: f.name, content: contentAndMetadata.content, metadata: contentAndMetadata.metadata)
    }

    static void generateGuidesPages(ClassLoader classLoader, File pages, String url) {
        List<Guide> guides = GuidesFetcher.fetchGuides()
        Set<Tag> tags = TagUtils.populateTags(guides)
        File pageOutput = new File(pages.getAbsolutePath() + "/" + PAGE_NAME_GUIDES)
        pageOutput.createNewFile()
        pageOutput.text = "title: Guides | Grails Framework\nbody: guides\nJAVASCRIPT: ${url}/javascripts/search.js\n---\n" +
                GuidesPage.mainContent(classLoader, guides, tags)

        File tagsDir = new File(pages.getAbsolutePath() + "/" + TAGS)
        tagsDir.mkdir()
        for (Tag tag : tags) {
            String slug = "${tag.slug.toLowerCase()}.html"
            pageOutput = new File(tagsDir.getAbsolutePath() + "/" + slug)
            pageOutput.createNewFile()
            pageOutput.text = "---\ntitle: Guides with tag: ${tag} | Grails Framework\nbody: guides\n---\n" + GuidesPage.mainContent(classLoader, guides, tags, null, tag)
        }
        File categoriesDir = new File(pages.getAbsolutePath() + "/" + CATEGORIES)
        categoriesDir.mkdir()
        for (Category category : GuidesPage.categories().values() ) {
            String slug = "${category.slug.toLowerCase()}.html"
            pageOutput = new File(categoriesDir.getAbsolutePath() + "/" + slug)
            pageOutput.createNewFile()
            pageOutput.text = "---\ntitle: Guides at category ${category.name} | Grails Framework\nbody: guides\n---\n" + GuidesPage.mainContent(classLoader, guides, tags, category, null)
        }
    }
}

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

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import website.gradle.GrailsWebsiteExtension
import website.model.Page
import website.model.documentation.SiteMap
import website.model.guides.GuidesFetcher
import website.model.guides.GuidesPage
import website.model.guides.TagUtils

@CompileStatic
@CacheableTask
abstract class GuidesTask extends GrailsWebsiteTask {

    @Internal
    final String description =
            'Generates guides home, tags and categories HTML pages - build/temp/index.html'

    public static final String NAME = 'genGuides'

    private static final String PAGE_NAME_GUIDES = 'guides.html'
    
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getDocument()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getReleases()

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

    static TaskProvider<GuidesTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, GuidesTask) {
            it.about.set(siteExt.description)
            it.document.set(siteExt.template)
            it.keywords.set(siteExt.keywords)
            it.outputDir.set(siteExt.outputDir)
            it.robots.set(siteExt.robots)
            it.releases.set(siteExt.releases)
            it.title.set(siteExt.title)
            it.url.set(siteExt.url)
        }
    }

    @TaskAction
    void renderGuides() {
        def tempDir = outputDir.dir('temp').get().asFile.tap { it.mkdirs() }
        generateGuidesPages(tempDir, url.get())

        def template = document.get().asFile
        def templateText = template.text
        def distDir = outputDir.dir('dist').get().asFile.tap { it.mkdirs() }

        def releasesFile = releases.get().asFile
        def latest = SiteMap.latestVersion(releasesFile)
        def olderVersions = SiteMap.olderVersions(releasesFile).reverse()
        def versions = olderVersions.collect {version -> "<option>$version</option>" }.join(' ')
        def meta = RenderSiteTask.siteMeta(
                title.get(),
                about.get(),
                url.get(),
                keywords.get(),
                robots.get(),
                latest.versionText,
                versions)
        def f = new File(tempDir, PAGE_NAME_GUIDES)
        def page = pageWithFile(f)
        page.filename = 'index.html'
        RenderSiteTask.renderPages(meta, [page], distDir, templateText)
        RenderSiteTask.renderPages(
                meta,
                parseCategoryPages(tempDir),
                new File(distDir, 'categories').tap { it.mkdirs() },
                templateText
        )
        RenderSiteTask.renderPages(
                meta,
                parseTagsPages(tempDir),
                new File(distDir, 'tags').tap { it.mkdirs() },
                templateText
        )
    }

    static List<Page> parseCategoryPages(File pages) {
        List<Page> listOfPages = []
        new File(pages, 'categories').eachFile { categoryFile ->
            listOfPages << pageWithFile(categoryFile)
        }
        listOfPages
    }

    static List<Page> parseTagsPages(File pages) {
        List<Page> listOfPages = []
        new File(pages, 'tags').eachFile { tagFile ->
            listOfPages << pageWithFile(tagFile)
        }
        listOfPages
    }

    static Page pageWithFile(File f) {
        def contentAndMetadata = RenderSiteTask.parseFile(f)
        new Page(
                filename: f.name,
                content: contentAndMetadata.content,
                metadata: contentAndMetadata.metadata
        )
    }

    static void generateGuidesPages(File pages, String url) {
        def guides = GuidesFetcher.fetchGuides()
        def tags = TagUtils.populateTags(guides)
        new File(pages, PAGE_NAME_GUIDES).setText(
                "title: Guides | Grails Framework\nbody: guides\nJAVASCRIPT: $url/javascripts/search.js\n---\n" +
                        GuidesPage.mainContent(guides, tags),
                'UTF-8'

        )

        def tagsDir = new File(pages, 'tags').tap { it.mkdirs() }
        for (def tag : tags) {
            def slug = "${tag.slug.toLowerCase()}.html"
            new File(tagsDir, slug).setText(
                    "---\ntitle: Guides with tag: $tag.title | Grails Framework\nbody: guides\n---\n" +
                            GuidesPage.mainContent(guides, tags, null, tag),
                    'UTF-8'

            )
        }
        def categoriesDir = new File(pages, 'categories').tap { it.mkdirs() }
        for (def category : GuidesPage.categories.values()) {
            def slug = "${category.slug.toLowerCase()}.html"
            new File(categoriesDir, slug).setText(
                    "---\ntitle: Guides at category $category.name | Grails Framework\nbody: guides\n---\n" +
                            GuidesPage.mainContent(guides, tags, category, null),
                    'UTF-8'
            )
        }
    }
}

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

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
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

import org.grails.Page
import org.grails.documentation.SiteMap
import org.grails.gradle.GrailsWebsiteExtension
import org.grails.guides.GuidesFetcher
import org.grails.guides.GuidesPage
import org.grails.guides.TagUtils

@CompileStatic
@CacheableTask
class GuidesTask extends GrailsWebsiteTask {

    @Internal
    String description = 'Generates guides home, tags and categories HTML pages - build/temp/index.html'

    public static final String NAME = 'genGuides'

    private static final String PAGE_NAME_GUIDES = 'guides.html'
    
    private ObjectFactory objects

    @Inject
    GuidesTask(ObjectFactory objects) {
        this.objects = objects
    }
    
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty document = objects.fileProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty releases = objects.fileProperty()

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
        def tempDir = new File(outputDir.get().asFile, 'temp').tap { it.mkdirs() }
        def classLoader = getClass().classLoader

        generateGuidesPages(classLoader, tempDir, url.get())
        def template = document.get().asFile
        def templateText = template.text
        def distDir = new File(outputDir.get().asFile, 'dist').tap { it.mkdirs() }

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

    static void generateGuidesPages(ClassLoader classLoader, File pages, String url) {
        def guides = GuidesFetcher.fetchGuides()
        def tags = TagUtils.populateTags(guides)
        def pageOutput = new File(pages, PAGE_NAME_GUIDES)
        pageOutput.createNewFile()
        pageOutput.text = "title: Guides | Grails Framework\nbody: guides\nJAVASCRIPT: ${url}/javascripts/search.js\n---\n" +
                GuidesPage.mainContent(classLoader, guides, tags)

        def tagsDir = new File(pages, 'tags').tap { it.mkdir() }
        for (def tag : tags) {
            def slug = "${tag.slug.toLowerCase()}.html"
            pageOutput = new File(tagsDir, slug)
            pageOutput.createNewFile()
            pageOutput.text = "---\ntitle: Guides with tag: ${tag} | Grails Framework\nbody: guides\n---\n" +
                    GuidesPage.mainContent(classLoader, guides, tags, null, tag)
        }
        def categoriesDir = new File(pages, 'categories').tap { it.mkdir() }
        for (def category : GuidesPage.categories.values()) {
            def slug = "${category.slug.toLowerCase()}.html"
            pageOutput = new File(categoriesDir, slug)
            pageOutput.createNewFile()
            pageOutput.text = "---\ntitle: Guides at category ${category.name} | Grails Framework\nbody: guides\n---\n" +
                    GuidesPage.mainContent(classLoader, guides, tags, category, null)
        }
    }
}

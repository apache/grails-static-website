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
package org.grails.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin

@CompileStatic
class GrailsWebsitePlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "grails"
    public static final String TASK_GEN_DOCS = "genDocs"
    public static final String TASK_GEN_DOWNLOADS = "genDownloads"
    public static final String TASK_GEN_PROFILES = "genProfiles"
    public static final String TASK_GEN_GUIDES = "genGuides"
    public static final String TASK_GEN_FAQ = "genFaq"
    public static final String TASK_BUILD = "build"
    public static final String TASK_GEN_SITE = "renderSite"
    public static final String TASK_GEN_SITEMAP = "genSitemap"
    public static final String TASK_GEN_PLUGINS = "genPlugins"
    public static final String TASK_COPY_ASSETS = "copyAssets"
    public static final String BUILD_GUIDES = "buildGuides"
    public static final String GROUP_GRAILS = 'grails'
    public static final String TASK_RENDER_BLOG = 'renderBlog'
    public static final String TASK_RENDER_MINUTES = 'renderMinutes'
    public static final String TASK_GEN_HTACCESS = "genHtaccess"


    @Override
    void apply(Project project) {
        project.getPlugins().apply(BasePlugin.class)
        project.extensions.create(EXTENSION_NAME, SiteExtension)
        project.tasks.register(TASK_GEN_PROFILES, ProfilesTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("profiles", siteExtension.profiles)
                task.setProperty("output", siteExtension.output)
            }
            task.setDescription('Generates profiles HTML page - build/temp/profiles.html')
            task.setGroup(GROUP_GRAILS)
        })
        project.tasks.register(TASK_GEN_DOCS, DocumentationTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("modules", siteExtension.modules)
                task.setProperty("releases", siteExtension.releases)
                task.setProperty("output", siteExtension.output)
                task.setProperty("url", siteExtension.url)
            }
            task.setDescription('Generates documentation HTML page - build/temp/documentation.html')
            task.setGroup(GROUP_GRAILS)
        })
        project.tasks.register(TASK_GEN_DOWNLOADS, DownloadTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("releases", siteExtension.releases)
                task.setProperty("output", siteExtension.output)
                task.setProperty("url", siteExtension.url)
            }
            task.setDescription('Generates download HTML page - build/temp/download.html')
            task.setGroup(GROUP_GRAILS)
        })

        project.tasks.register(TASK_GEN_PLUGINS, PluginsTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("output", siteExtension.output)
                task.setProperty("url", siteExtension.url)
                task.setProperty("document", siteExtension.template)
            }
            task.setGroup(GROUP_GRAILS)
            task.setDescription('Generates an HTML Page listing the Grails plugins')
        })


        project.tasks.register(TASK_GEN_SITEMAP, SitemapTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("output", siteExtension.output)
                task.setProperty("url", siteExtension.url)
            }
            task.setGroup(GROUP_GRAILS)
            task.setDescription('Generates build/dist/sitemap.xml with every page in the site')
        })
        project.tasks.register(TASK_RENDER_BLOG, BlogTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("url", siteExtension.url)
                task.setProperty("title", siteExtension.title)
                task.setProperty("about", siteExtension.description)
                task.setProperty("keywords", siteExtension.keywords)
                task.setProperty("robots", siteExtension.robots)
                task.setProperty("document", siteExtension.template)
                task.setProperty("output", siteExtension.output)
                task.setProperty("posts", siteExtension.posts)
                task.setProperty("assets", siteExtension.assets)
                task.setProperty("releases", siteExtension.releases)
            }
            task.setGroup(GROUP_GRAILS)
            task.description = 'Renders Markdown posts (posts/*.md) into HTML pages (dist/blog/*.html). It generates tag pages. Generates RSS feed. Posts with future dates are not generated.'
        })
        project.tasks.register(TASK_RENDER_MINUTES, MinutesTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("url", siteExtension.url)
                task.setProperty("title", siteExtension.title)
                task.setProperty("about", siteExtension.description)
                task.setProperty("keywords", siteExtension.keywords)
                task.setProperty("robots", siteExtension.robots)
                task.setProperty("document", siteExtension.template)
                task.setProperty("output", siteExtension.output)
                task.setProperty("assets", siteExtension.assets)
                task.setProperty("minutes", siteExtension.minutes)
                task.setProperty("releases", siteExtension.releases)
            }
            task.setGroup(GROUP_GRAILS)
            task.description = 'Renders Markdown minutes (minutes/*.md) into HTML pages (dist/foundation/minutes/*.html). It generates tag pages. Generates RSS feed. Minutes with future dates are not generated.'
        })
        project.tasks.register(TASK_GEN_FAQ, QuestionsTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("questions", siteExtension.questions)
                task.setProperty("output", siteExtension.output)
            }
            task.setDescription("Generates FAQ HTML - build/temp/faq.html ")
            task.setGroup(GROUP_GRAILS)
        })

        project.tasks.register(TASK_GEN_GUIDES, GuidesTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("url", siteExtension.url)
                task.setProperty("title", siteExtension.title)
                task.setProperty("about", siteExtension.description)
                task.setProperty("keywords", siteExtension.keywords)
                task.setProperty("robots", siteExtension.robots)
                task.setProperty("document", siteExtension.template)
                task.setProperty("output", siteExtension.output)
                task.setProperty("releases", siteExtension.releases)
            }
            task.setDescription('Generates guides home, tags and categories HTML pages - build/temp/index.html')
            task.setGroup(GROUP_GRAILS)
        })
        project.tasks.register(TASK_COPY_ASSETS, CopyAssetsTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("output", siteExtension.output)
                task.setProperty("assets", siteExtension.assets)
            }
            task.setDescription('Copies css, js, fonts and images from the assets folder to the dist folder')
            task.setGroup(GROUP_GRAILS)
        })

        project.tasks.register(TASK_GEN_HTACCESS, HtaccessTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("output", siteExtension.output)
                task.setProperty("url", siteExtension.url)
            }
            task.setDescription('Generates .htaccess file in the dist folder for Apache web server configuration')
            task.setGroup(GROUP_GRAILS)
        })

        project.tasks.register(BUILD_GUIDES, BuildGuidesTask, { task ->
            task.dependsOn(TASK_COPY_ASSETS)
            task.dependsOn(TASK_GEN_GUIDES)
            task.setGroup(GROUP_GRAILS)
            task.finalizedBy(TASK_GEN_SITEMAP)
            task.setDescription('Build guides website - generates guides pages, copies assets and generates a sitemap')
        })
        project.tasks.register(TASK_GEN_SITE, RenderSiteTask, { task ->
            Object extension = project.getExtensions().findByName(EXTENSION_NAME)
            if (extension instanceof SiteExtension) {
                SiteExtension siteExtension = ((SiteExtension) extension)
                task.setProperty("url", siteExtension.url)
                task.setProperty("title", siteExtension.title)
                task.setProperty("about", siteExtension.description)
                task.setProperty("keywords", siteExtension.keywords)
                task.setProperty("robots", siteExtension.robots)
                task.setProperty("document", siteExtension.template)
                task.setProperty("output", siteExtension.output)
                task.setProperty("pages", siteExtension.pages)
                task.setProperty("releases", siteExtension.releases)
            }
            task.setGroup(GROUP_GRAILS)
            task.dependsOn(TASK_COPY_ASSETS)
            task.dependsOn(TASK_GEN_DOCS)
            task.dependsOn(TASK_GEN_DOWNLOADS)
            task.dependsOn(TASK_GEN_PROFILES)
            task.dependsOn(TASK_GEN_FAQ)
            task.finalizedBy(TASK_RENDER_BLOG)
            task.finalizedBy(TASK_GEN_PLUGINS)
            task.finalizedBy(TASK_RENDER_MINUTES)
            task.finalizedBy(TASK_GEN_SITEMAP)
            task.finalizedBy(TASK_GEN_HTACCESS)
            task.setDescription('Build Grails website - generates pages with HTML entries in pages and build/temp, renders blog and RSS feed, copies assets and generates a sitemap')

        })
        project.tasks.named(TASK_BUILD).configure(new Action<Task>() {
            @Override
            void execute(Task task) {
                task.dependsOn(TASK_GEN_SITE)
            }
        })
    }
}

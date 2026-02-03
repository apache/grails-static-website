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
package website.gradle

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

import website.gradle.tasks.BlogTask

import website.gradle.tasks.AssetsTask
import website.gradle.tasks.DocumentationTask
import website.gradle.tasks.DownloadTask
import website.gradle.tasks.GrailsWebsiteTask
import website.gradle.tasks.GuidesTask
import website.gradle.tasks.HtaccessTask
import website.gradle.tasks.MinutesTask
import website.gradle.tasks.PluginsTask
import website.gradle.tasks.ProfilesTask
import website.gradle.tasks.QuestionsTask
import website.gradle.tasks.RenderSiteTask
import website.gradle.tasks.SitemapTask

@CompileStatic
class GrailsWebsitePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply('base')

        GrailsWebsiteExtension siteExt = project.extensions.create(
                GrailsWebsiteExtension.NAME,
                GrailsWebsiteExtension
        )

        AssetsTask.register(project, siteExt)
        BlogTask.register(project, siteExt)
        DocumentationTask.register(project, siteExt)
        DownloadTask.register(project, siteExt)
        GuidesTask.register(project, siteExt)
        HtaccessTask.register(project, siteExt)
        MinutesTask.register(project, siteExt)
        PluginsTask.register(project, siteExt)
        ProfilesTask.register(project, siteExt)
        QuestionsTask.register(project, siteExt)

        SitemapTask.register(project, siteExt).configure {

            // SitemapTask must run after all tasks that generate files in dist/

            it.dependsOn(BlogTask.NAME)
            it.dependsOn(MinutesTask.NAME)
            it.dependsOn(PluginsTask.NAME)
            it.dependsOn(HtaccessTask.NAME)
        }

        RenderSiteTask.register(project, siteExt).configure {

            // The Grails Guides are not part of this site, they are published to https://guides.grails.org
            // and hosted at gh-pages in https://github.com/grails/grails-guides-template
            // The buildGuides task can be used to generate the guide site.

            it.dependsOn(AssetsTask.NAME)
            it.dependsOn(DocumentationTask.NAME)
            it.dependsOn(DownloadTask.NAME)
            it.dependsOn(ProfilesTask.NAME)
            it.dependsOn(QuestionsTask.NAME)

            it.finalizedBy(BlogTask.NAME)
            it.finalizedBy(MinutesTask.NAME)
            it.finalizedBy(HtaccessTask.NAME)
            it.finalizedBy(PluginsTask.NAME)
            it.finalizedBy(SitemapTask.NAME)
        }

        project.tasks.register('buildGuides') {
            // Task for only generating the Grails Guides which are published to https://guides.grails.org
            // and hosted at gh-pages in https://github.com/grails/grails-guides-template
            it.description = 'Build guides website - generates guides pages, copies assets and generates a sitemap'
            it.group = GrailsWebsiteTask.GROUP
            it.dependsOn(AssetsTask.NAME)
            it.dependsOn(GuidesTask.NAME)
            it.finalizedBy(SitemapTask.NAME)

        }

        project.tasks.named('build') {
            it.dependsOn(RenderSiteTask.NAME)
        }
    }
}

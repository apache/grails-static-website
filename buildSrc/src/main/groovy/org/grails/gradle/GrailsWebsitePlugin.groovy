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

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.grails.gradle.tasks.BlogTask
import org.grails.gradle.tasks.BuildGuidesTask
import org.grails.gradle.tasks.CopyAssetsTask
import org.grails.gradle.tasks.DocumentationTask
import org.grails.gradle.tasks.DownloadTask
import org.grails.gradle.tasks.GuidesTask
import org.grails.gradle.tasks.HtaccessTask
import org.grails.gradle.tasks.MinutesTask
import org.grails.gradle.tasks.PluginsTask
import org.grails.gradle.tasks.ProfilesTask
import org.grails.gradle.tasks.QuestionsTask
import org.grails.gradle.tasks.RenderSiteTask
import org.grails.gradle.tasks.SitemapTask

@CompileStatic
class GrailsWebsitePlugin implements Plugin<Project> {

    public static final String TASK_RENDER_SITE = 'renderSite'

    @Override
    void apply(Project project) {
        project.pluginManager.apply('base')

        GrailsWebsiteExtension siteExt = project.extensions.create(
                GrailsWebsiteExtension.NAME,
                GrailsWebsiteExtension
        )

        BlogTask.register(project, siteExt)
        CopyAssetsTask.register(project, siteExt)
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

        BuildGuidesTask.register(project, siteExt).configure {
            it.dependsOn(CopyAssetsTask.NAME)
            it.dependsOn(GuidesTask.NAME)
            it.finalizedBy(SitemapTask.NAME)

        }

        RenderSiteTask.register(project, siteExt).configure {
            it.dependsOn(CopyAssetsTask.NAME)
            it.dependsOn(DocumentationTask.NAME)
            it.dependsOn(DownloadTask.NAME)
            it.dependsOn(GuidesTask.NAME)
            it.dependsOn(ProfilesTask.NAME)
            it.dependsOn(QuestionsTask.NAME)
            it.finalizedBy(BlogTask.NAME)
            it.finalizedBy(PluginsTask.NAME)
            it.finalizedBy(MinutesTask.NAME)
            it.finalizedBy(SitemapTask.NAME)
            it.finalizedBy(HtaccessTask.NAME)
        }

        project.tasks.named('build') {
            it.dependsOn(TASK_RENDER_SITE)
        }
    }
}

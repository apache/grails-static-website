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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.grails.gradle.GrailsWebsiteExtension

import static groovy.io.FileType.FILES

@CompileStatic
@CacheableTask
class SitemapTask extends GrailsWebsiteTask {

    @Internal
    String description = 'Generates build/dist/sitemap.xml with every page in the site'

    public static final String NAME = 'genSitemap'

    private final ObjectFactory objects

    @Inject
    SitemapTask(ObjectFactory objects) {
        this.objects = objects
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty inputDir = objects.directoryProperty()

    @Input
    final Property<String> url = objects.property(String)

    @OutputFile
    final RegularFileProperty outputFile = objects.fileProperty()

    static TaskProvider<SitemapTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, SitemapTask) {
            it.inputDir.set(siteExt.outputDir.dir('dist'))
            it.url.set(siteExt.url)
            it.outputFile.set(siteExt.outputDir.file('dist/sitemap.xml'))
        }
    }

    @TaskAction
    void renderSitemap() {
        def websiteUrl = url.get()
        List<String> urls = []
        def inputDirectory = inputDir.get().asFile
        inputDirectory.eachFileRecurse(FILES) {
            if (it.name.endsWith('.html')) {
                urls.add(websiteUrl + it.absolutePath.replace(inputDirectory.absolutePath, ''))
            }
        }
        outputFile.get().asFile.with {
            parentFile.mkdirs()
            text = sitemapContent(urls.sort())
        }
    }

    @CompileDynamic
    static String sitemapContent(List<String> urls) {
        def writer = new StringWriter()
        def html = new MarkupBuilder(writer)
        html.urlset(xmlns: 'https://www.sitemaps.org/schemas/sitemap/0.9') {
            for (def urlStr : urls) {
                url {
                    loc(urlStr)
                }
            }
        }
        '<?xml version="1.0" encoding="UTF-8"?>\n' + writer.toString()
    }
}

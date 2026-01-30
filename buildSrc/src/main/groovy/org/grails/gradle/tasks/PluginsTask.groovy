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

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import javax.inject.Inject

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

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

import org.grails.gradle.GrailsWebsiteExtension
import org.grails.plugin.Owner
import org.grails.plugin.Plugin
import org.grails.plugin.PluginsPage

@Slf4j
@CompileStatic
@CacheableTask
class PluginsTask extends GrailsWebsiteTask {

    @Internal
    String description = 'Generates an HTML Page listing the Grails plugins'

    public static final String NAME = 'genPlugins'

    private static final String GRAILS_PLUGINS_JSON =
            'https://raw.githubusercontent.com/grails/grails-plugins-metadata/main/grails-plugins.json'

    private final ObjectFactory objects

    @Inject
    PluginsTask(ObjectFactory objects) {
        this.objects = objects
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty document = objects.fileProperty()

    @Input
    final ListProperty<String> keywords = objects.listProperty(String)

    @Input
    final Property<String> url = objects.property(String)

    @OutputDirectory
    final DirectoryProperty outputDir = objects.directoryProperty()

    static TaskProvider<PluginsTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, PluginsTask) {
            it.document.set(siteExt.template)
            it.outputDir.set(siteExt.outputDir)
            it.url.set(siteExt.url)
        }
    }

    @TaskAction
    void renderPluginsPage() {
        def template = document.get().asFile
        def templateText = template.text
        def metadata = RenderSiteTask.siteMeta(
                'Grails Plugins', // TODO Make it configurable
                'List of Plugins', // TODO Make it configurable
                url.get(),
                keywords.get(),
                'all', // TODO Make it configurable,
                '',
                ''
        )
        metadata.put('JAVASCRIPT', '/javascripts/plugins-search.js')

        def resolvedMetadata = RenderSiteTask.processMetadata(metadata)
        def result = new JsonSlurper().parse(GRAILS_PLUGINS_JSON.toURL())
        def plugins = pluginsFromJson(result)
        renderHtml(plugins, templateText, resolvedMetadata, 'plugins.html')
    }

    void renderHtml(List<Plugin> plugins, String templateText, Map<String, String> metadata, String fileName) {
        def siteUrl = url.get()
        def distDir = new File(outputDir.get().asFile, 'dist').tap { it.mkdir() }
        def pluginsDir = new File(distDir, 'plugins').tap { it.mkdir() }
        def pluginsTagsDir = new File(pluginsDir, 'tags').tap { it.mkdir() }
        def pluginsOwnersDir = new File(pluginsDir, 'owners').tap { it.mkdir() }
        def pluginOutputFile = new File(distDir, fileName).tap { it.createNewFile() }
        def html = PluginsPage.mainContent(siteUrl, plugins, 'Grails Plugins', null)
        html = RenderSiteTask.renderHtmlWithTemplateContent(html, metadata, templateText)
        html = RenderSiteTask.highlightMenu(html, metadata, '/plugins.html')
        pluginOutputFile.text = html

        def tags = plugins.stream().flatMap(p -> p.labels.stream()).distinct().collect(Collectors.toList())
        for (def tag in tags) {
            def tagsOutputFile = new File(pluginsTagsDir, "${tag}.html").tap { it.createNewFile() }
            def htmlForTags = renderHtmlPagesForTags(siteUrl, plugins, tag)
            tagsOutputFile.text = RenderSiteTask.renderHtmlWithTemplateContent(htmlForTags, metadata, templateText)
        }

        def owners = plugins.stream().map(p -> p.owner.name).distinct().collect(Collectors.toList())
        for (def owner in owners) {
            def ownersOutputFile = new File(pluginsOwnersDir, "${owner}.html").tap { it.createNewFile() }
            def htmlForOwnersFile = renderHtmlPagesForOwners(siteUrl, plugins, owner)
            ownersOutputFile.text = RenderSiteTask.renderHtmlWithTemplateContent(htmlForOwnersFile, metadata, templateText)
        }
    }

    static String renderHtmlPagesForTags(String siteUrl, List<Plugin> plugins, String tag) {
        def filteredPlugins = plugins.stream().filter(p -> p.labels.contains(tag)).collect(Collectors.toList())
        PluginsPage.mainContent(siteUrl, plugins , "Plugins by tag #${tag}", filteredPlugins)
    }

    static String renderHtmlPagesForOwners(String siteUrl, List<Plugin> plugins, String owner) {
        def filteredPlugins = plugins.stream().filter(p -> p.owner.name == owner).collect(Collectors.toList())
        PluginsPage.mainContent(siteUrl, plugins , "Plugins by creator: #${owner}", filteredPlugins)
    }

    @CompileDynamic
    List<Plugin> pluginsFromJson(Object json) {
        List<Plugin> plugins = []
        for (int i = 0; i < json.size(); i++) {
            def owner = new Owner(name: json[i].bintrayPackage.owner)
            def updatedDate = parseIsoStringToDate(json[i].bintrayPackage.updated)
            plugins.add(
                    new Plugin(
                            name: json[i].bintrayPackage.name,
                            updated: updatedDate,
                            owner: owner,
                            desc: json[i].bintrayPackage.desc,
                            vcsUrl: json[i].bintrayPackage.vcsUrl,
                            latestVersion: json[i].bintrayPackage.latestVersion,
                            githubStars: githubStars(json[i].bintrayPackage.vcsUrl).orElse(null),
                            labels: json[i].bintrayPackage.labels as List<String>
                    )
            )
        }
        Collections.unmodifiableList(plugins)
    }

    static LocalDateTime parseIsoStringToDate(String isoFormattedString) {
        def f = DateTimeFormatter.ofPattern(/yyyy-MM-dd'T'HH:mm:ss.SSSXXX/)
        LocalDateTime.parse(isoFormattedString, f)
    }

    @CompileDynamic
    static Optional<Integer> githubStars(String vcsUrl) {
        if (!vcsUrl) {
            return Optional.empty()
        }
        if (!vcsUrl.contains('github.com')) {
            return Optional.empty()
        }
        if (!System.getenv('GH_TOKEN')) {
            return Optional.empty()
        }
        try {
            log.debug('Fetching github stars of {}', vcsUrl)
            def url = 'https://api.github.com/repos/' + vcsUrl.substring(vcsUrl.indexOf('github.com/') + 'github.com/'.length())
            def request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header('X-GitHub-Api-Version', '2022-11-28')
                    .header('Authorization', "Bearer ${System.getenv("GH_TOKEN")}")
                    .GET()
                    .build()
            def response = HttpClient.newBuilder().build().send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            )
            if (response.statusCode() == 200) {
                def json = response.body()
                def stars = (new JsonSlurper().parseText(json).stargazers_count) as Integer
                return (stars == 0 ? Optional.empty() : Optional.of(stars)) as Optional<Integer>
            }
        } catch (Exception e) {
            log.error("Error fetching GitHub stars for $vcsUrl", e)
            return Optional.empty()
        }
        return Optional.empty()
    }
}

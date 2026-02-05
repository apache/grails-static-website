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

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import jakarta.annotation.Nullable

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
import website.model.plugin.Owner
import website.model.plugin.Plugin
import website.model.plugin.PluginVersion
import website.model.plugin.PluginsPage

@Slf4j
@CompileStatic
@CacheableTask
abstract class PluginsTask extends GrailsWebsiteTask {

    @Internal
    final String description = 'Generates an HTML Page listing the Grails plugins'

    public static final String NAME = 'genPlugins'

    private static final String GRAILS_PLUGINS_JSON =
            'https://raw.githubusercontent.com/grails/grails-plugins-metadata/main/grails-plugins-index.json'

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getDocument()

    @Input
    abstract ListProperty<String> getKeywords()

    @Input
    abstract Property<String> getUrl()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

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
        def metadata = RenderSiteTask.siteMeta(
                'Grails Plugins', // TODO Make it configurable
                'List of Plugins', // TODO Make it configurable
                url.get(),
                keywords.get(),
                'all', // TODO Make it configurable,
                '',
                ''
        )
        metadata.put('JAVASCRIPT', '[%url]/javascripts/plugins-search.js')

        def resolvedMetadata = RenderSiteTask.processMetadata(metadata)
        def result = new JsonSlurper().parse(GRAILS_PLUGINS_JSON.toURL()) as List<Object>
        def plugins = pluginsFromJson(result)
        renderHtml(plugins, document.get().asFile.text, resolvedMetadata, 'plugins.html')
    }

    void renderHtml(List<Plugin> plugins, String templateText, Map<String, String> metadata, String fileName) {
        def siteUrl = url.get()

        def distDir          = outputDir.dir('dist').get().asFile
        def pluginsDir       = outputDir.dir('dist/plugins').get().asFile
        def pluginsTagsDir   = outputDir.dir('dist/plugins/tags').get().asFile.tap { mkdirs() }
        def pluginsOwnersDir = outputDir.dir('dist/plugins/owners').get().asFile.tap { mkdirs() }

        def wrap = { String html -> RenderSiteTask.renderHtmlWithTemplateContent(html, metadata, templateText) }

        // main plugins page
        new File(distDir, fileName).setText(
                RenderSiteTask.highlightMenu(
                        wrap(PluginsPage.mainContent(siteUrl, plugins, 'Grails Plugins', null)),
                        metadata,
                        '/plugins.html'
                ),
                'UTF-8'
        )

        // tag pages
        (plugins ?: [])
                .collectMany { it.labels ?: ([] as List<String>) }
                .unique()
                .each { tag ->
                    new File(pluginsTagsDir, "${tag}.html").setText(
                            wrap(renderHtmlPagesForTags(siteUrl, plugins, tag)),
                            'UTF-8'
                    )
                }

        // owner pages
        (plugins ?: [])
                .collect { it.owner?.name }
                .findAll { it }
                .unique()
                .each { owner ->
                    new File(pluginsOwnersDir, "${owner}.html").setText(
                            wrap(renderHtmlPagesForOwners(siteUrl, plugins, owner)),
                            'UTF-8'
                    )
                }
    }

    static String renderHtmlPagesForTags(String siteUrl, List<Plugin> plugins, String tag) {
        def filteredPlugins = (plugins ?: []).findAll { (it.labels ?: []).contains(tag) }
        PluginsPage.mainContent(siteUrl, plugins, "Plugins by tag #$tag", filteredPlugins)
    }

    static String renderHtmlPagesForOwners(String siteUrl, List<Plugin> plugins, String owner) {
        def filteredPlugins = (plugins ?: []).findAll { it.owner?.name == owner }
        PluginsPage.mainContent(siteUrl, plugins, "Plugins by creator: #$owner", filteredPlugins)
    }

    /**
     * Converts the JSON array from grails-plugins-index.json into a list of {@link Plugin} objects.
     * Extracts the latest version and update date from the first entry in each plugin's versions array.
     *
     * @param json the parsed JSON array from the plugins index
     * @return list of Plugin objects
     */
    @CompileDynamic
    static List<Plugin> pluginsFromJson(List json) {
        (json ?: []).collect { item ->
            def versionsJson = item['versions'] as List
            def latestVersionEntry = versionsJson?.first()

            // Parse all versions
            def versions = (versionsJson ?: []).collect { v ->
                new PluginVersion(
                    version: v['version'],
                    date: v['date'] ? parseIsoStringToDate(v['date'] as String) : null,
                    grailsVersion: v['grailsVersion']
                )
            } as List<PluginVersion>

            new Plugin(
                    name: item['name'],
                    desc: item['desc'],
                    coords: item['coords'],
                    vcsUrl: item['vcs'],
                    docsUrl: item['docs'],
                    mavenRepo: item['maven-repo'],
                    owner: new Owner(name: item['owner']),
                    latestVersion: latestVersionEntry?.version,
                    grailsVersion: latestVersionEntry?.grailsVersion,
                    updated: latestVersionEntry?.date ? parseIsoStringToDate(latestVersionEntry.date as String) : null,
                    githubStars: githubStars(item['vcs'] as String),
                    labels: (item['labels'] ?: []) as List<String>,
                    license: (item['licenses'] as List)?.first(),
                    deprecated: item['deprecated'] as String,
                    versions: versions
            )
        }
    }

    /**
     * Parses an ISO 8601 formatted date string into a {@link LocalDateTime}.
     *
     * @param isoFormattedString the date string (e.g., "2025-12-19T15:35:33+0000")
     * @return parsed LocalDateTime
     */
    static LocalDateTime parseIsoStringToDate(String isoFormattedString) {
        def f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        LocalDateTime.parse(isoFormattedString, f)
    }

    /**
     * Fetches the GitHub star count for a repository using the GitHub API.
     * Requires the GH_TOKEN environment variable to be set.
     *
     * @param vcsUrl the GitHub repository URL
     * @return the number of stars, or {@code null} if unavailable or not a GitHub repo
     */
    @Nullable
    @CompileDynamic
    static Integer githubStars(String vcsUrl) {
        def token = System.getenv('GH_TOKEN')
        if (!vcsUrl || !vcsUrl.contains('github.com') || !token) {
            return null
        }
        try {
            log.info('Fetching github stars of {}', vcsUrl)
            def repoPath = vcsUrl.replaceFirst(/^.*github\.com\//, '')
            def url = "https://api.github.com/repos/$repoPath"
            def request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header('X-GitHub-Api-Version', '2022-11-28')
                    .header('Authorization', "Bearer $token")
                    .GET()
                    .build()
            def response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                return null
            }
            def stars = new JsonSlurper()
                    .parseText(response.body())['stargazers_count'] as Integer
            return stars ?: null // 0 → null
        } catch (Exception e) {
            log.error("Error fetching GitHub stars for $vcsUrl", e)
            return null
        }
    }
}

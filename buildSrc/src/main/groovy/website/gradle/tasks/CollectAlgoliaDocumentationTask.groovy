/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.gradle.tasks

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import website.gradle.GrailsWebsiteExtension
import website.model.documentation.SiteMap

@CompileStatic
abstract class CollectAlgoliaDocumentationTask extends GrailsWebsiteTask {

    static final String NAME = 'collectAlgoliaDocumentation'

    private static final Set<String> DOCUMENTATION_EXCLUSIONS = [
            '0.6' // Grails 0.6 has no published documentation under grails.apache.org/docs.
    ] as Set

    @Internal
    final String description =
            'Collects published Grails User and API Documentation for Algolia indexing'

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getReleases()

    @Input
    abstract Property<String> getDocumentationBaseUrl()

    @Input
    abstract Property<String> getVersionSelection()

    @Input
    abstract Property<Integer> getMaxPagesPerVersion()

    @Input
    abstract Property<Boolean> getCollectExternal()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    static TaskProvider<CollectAlgoliaDocumentationTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, CollectAlgoliaDocumentationTask) {
            it.releases.set(
                    siteExt.releases
            )
            it.documentationBaseUrl.set(
                    project.providers
                            .environmentVariable('ALGOLIA_DOCUMENTATION_BASE_URL')
                            .orElse('https://grails.apache.org/docs'))
            it.versionSelection.set(
                    project.providers
                            .environmentVariable('ALGOLIA_DOCUMENTATION_VERSIONS')
                            .orElse(''))
            it.maxPagesPerVersion.set(
                    project.providers
                            .environmentVariable('ALGOLIA_DOCUMENTATION_MAX_PAGES')
                            .map { Integer.parseInt(it) }
                            .orElse(5000))
            it.collectExternal.set(
                    project.providers
                            .environmentVariable('ALGOLIA_COLLECT_EXTERNAL_DOCUMENTATION')
                            .map { Boolean.parseBoolean(it) }
                            .orElse(false))
            it.outputFile.set(
                    project.layout.buildDirectory.file('generated/algolia/documentation-records.json')
            )
        }
    }

    @TaskAction
    void collect() {
        if (!collectExternal.get()) {
            def outputFile = this.outputFile.get().asFile
            outputFile.parentFile.mkdirs()
            outputFile.text = '[]\n'
            logger.lifecycle(
                    'External Algolia documentation collection is disabled. ' +
                            'Set ALGOLIA_COLLECT_EXTERNAL_DOCUMENTATION=true to enable it.')
            return
        }
        def versions = selectedVersions()
        def records = [] as List<Map<String, Object>>
        for (def version : versions) {
            records.addAll(crawlVersion(version, 'guide', 'user-documentation'))
            records.addAll(crawlVersion(version, 'api', 'api'))
        }
        if (versions && records.isEmpty()) {
            throw new IllegalStateException(
                    "No external documentation was collected for ${versions.size()} configured version(s). " +
                            'The Algolia index was not updated.')
        }
        records = records.unique { it.objectID as String }.sort { a, b ->
            (a.objectID as String) <=> (b.objectID as String)
        }
        def outputFile = this.outputFile.get().asFile
        outputFile.parentFile.mkdirs()
        writeRecords(outputFile, records)
        logger.lifecycle(
                'Collected {} external documentation record(s) for Algolia.',
                records.size()
        )
    }

    private static void writeRecords(File outputFile, List<Map<String, Object>> records) {
        def tempFile = new File(outputFile.parentFile, "${outputFile.name}.tmp")
        try {
            tempFile.withWriter('UTF-8') { writer ->
                writer << '[\n'
                records.eachWithIndex { Map<String, Object> record, int index ->
                    if (index > 0) writer << ',\n'
                    writer << JsonOutput.toJson(record)
                }
                writer << '\n]\n'
            }
            if (outputFile.exists() && !outputFile.delete()) {
                throw new IOException("Unable to replace ${outputFile}")
            }
            if (!tempFile.renameTo(outputFile)) {
                throw new IOException("Unable to move ${tempFile} to ${outputFile}")
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private List<String> selectedVersions() {
        def configuredVersion = versionSelection.get().trim()
        if (configuredVersion) {
            return configuredVersion.split(',')*.trim()
                    .findAll { it && !DOCUMENTATION_EXCLUSIONS.contains(it) }.unique()
        }
        // Keep the scheduled crawl bounded by default. Older stable versions
        // remain opt-in through ALGOLIA_DOCUMENTATION_VERSIONS.
        def versions = SiteMap.highestStablePerMajor(releases.get().asFile).values()*.versionText
        versions.addAll(SiteMap.latestPreReleasePerMajor(releases.get().asFile).values()*.versionText as Collection)
        versions.unique().findAll { !DOCUMENTATION_EXCLUSIONS.contains(it) }
    }

    private List<Map<String, Object>> crawlVersion(String version, String area, String source) {
        def root = normalizedBase() + '/' + encodePath(version) + '/' + area + '/'
        def pending = new ArrayDeque<String>()
        def visited = new LinkedHashSet<String>()
        seedUrls(root, area).each { pending.add(it) }
        def records = [] as List<Map<String, Object>>
        def hasFetchedPage = false
        while (!pending.isEmpty() && visited.size() < maxPagesPerVersion.get()) {
            String pageUrl = pending.removeFirst()
            if (!visited.add(pageUrl)) {
                continue
            }
            def htmlContent = fetch(pageUrl)
            if (htmlContent == null) {
                continue
            }
            hasFetchedPage = true
            def document = Jsoup.parse(htmlContent, pageUrl)
            records.addAll(
                    ExportAlgoliaIndexTask.recordsFromExternalDocument(document, pageUrl, source, version)
            )
            document.select('a[href]').each { Element link ->
                def nextLink = normalizeLink(pageUrl, link.attr('href'))
                if (nextLink && inScope(nextLink, root) && !visited.contains(nextLink)) {
                    pending.add(nextLink)
                }
            }
        }
        if (!hasFetchedPage) {
            def collectionName = source == 'user-documentation' ? 'User Documentation' : 'API Documentation'
            logger.warn(
                    'No published {} found for Grails {} at {}; skipping it.',
                    collectionName, version, root
            )
            return []
        }
        logger.lifecycle(
                'Collected {} {} record(s) for Grails {}.',
                records.size(), source, version
        )
        records
    }

    private static List<String> seedUrls(String root, String area) {
        if (area != 'api') {
            return [root]
        }
        // Groovydoc starts with a frameset, so seed the frames containing the
        // package/class links as well as the root document.
        [root, root + 'index.html', root + 'overview-summary.html',
         root + 'overview-frame.html', root + 'allclasses-frame.html']
    }

    private String normalizedBase() {
        documentationBaseUrl.get().replaceAll('/+$', '')
    }

    private static String encodePath(String value) {
        URLEncoder.encode(value, 'UTF-8').replace('+', '%20')
    }

    private static String normalizeLink(String pageUrl, String href) {
        if (!href || href.startsWith('#') || href.startsWith('mailto:') ||
                href.startsWith('javascript:') || href.startsWith('data:')) {
            return null
        }
        try {
            def uri = URI.create(pageUrl).resolve(href.split('#', 2)[0])
            if (uri.scheme != 'https' && uri.scheme != 'http') return null
            return new URI(uri.scheme, uri.authority, uri.path, null, null).toString()
        } catch (Exception ignored) {
            return null
        }
    }

    private static boolean inScope(String url, String root) {
        url.startsWith(root) && !url.matches('(?i).+\\.(css|js|png|jpg|jpeg|gif|svg|ico|pdf|zip)$')
    }

    private static String fetch(String pageUrl) {
        HttpURLConnection connection = null
        try {
            connection = ((HttpURLConnection) new URI(pageUrl).toURL().openConnection()).tap {
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty('User-Agent', 'Apache-Grails-Algolia-Indexer/1.0')
            }
            int status = connection.responseCode
            if (status == HttpURLConnection.HTTP_NOT_FOUND || status == HttpURLConnection.HTTP_GONE) {
                return null
            }
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP ${status} while fetching ${pageUrl}")
            }
            connection.inputStream.getText('UTF-8')
        } catch (IOException ex) {
            throw new RuntimeException(ex.message, ex)
        } finally {
            connection?.disconnect()
        }
    }
}

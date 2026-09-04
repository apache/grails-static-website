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
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import java.security.MessageDigest

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.yaml.snakeyaml.Yaml

import website.gradle.GrailsWebsiteExtension
import website.model.documentation.SiteMap

@CompileStatic
abstract class ExportAlgoliaIndexTask extends GrailsWebsiteTask {

    static final String NAME = 'exportAlgoliaIndex'

    @Internal
    final String description =
            'Exports canonical website and documentation records for Algolia'

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getDistDir()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getExternalRecords()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getGuidesYml()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getReleases()

    @Input
    abstract Property<String> getSiteUrl()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    static TaskProvider<ExportAlgoliaIndexTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, ExportAlgoliaIndexTask) {
            it.distDir.set(siteExt.outputDir.dir('dist'))
            it.releases.set(siteExt.releases)
            it.siteUrl.set(siteExt.url)
            it.externalRecords.set(
                    project.layout.buildDirectory.file('generated/algolia/documentation-records.json')
            )
            it.guidesYml.set(
                    project.layout.projectDirectory.file('conf/guides.yml')
            )
            it.outputFile.set(
                    project.layout.buildDirectory.file('generated/algolia/records.json')
            )
        }
    }

    @TaskAction
    void export() {
        def rootDir = distDir.get().asFile
        def records = [] as List<Map<String, Object>>
        def versionRanks = loadVersionRanks(releases.get().asFile)
        if (rootDir.isDirectory()) {
            def guideMetadata = loadGuideMetadata(guidesYml.get().asFile)
            rootDir.eachFileRecurse { File file ->
                if (file.isFile() && file.name.endsWith('.html')) {
                    def relative = rootDir.toPath().relativize(file.toPath()).toString().replace('\\', '/')
                    def url = siteUrl.get().replaceAll('/+$', '') + '/' + relative
                    def document = Jsoup.parse(file, 'UTF-8', url)
                    def localRecords = recordsFromLocalDocument(document, relative, url)
                    if (relative ==~ 'guides/[^/]+/[^/]+/guide/index\\.html') {
                        enrichGuideRecords(localRecords, relative, guideMetadata)
                    }
                    records.addAll(localRecords)
                }
            }
        }
        records.each { Map<String, Object> record ->
            record.sourceRank = sourceRank(record.source as String)
            record.versionRank = versionRank(record.grailsVersion as String, versionRanks)
            record.sortKey = record.objectID
        }
        def byUrl = [:] as Map<String, Map<String, Object>>
        records.findAll { it.objectID && it.title && it.content }.each { Map<String, Object> record ->
            byUrl.putIfAbsent(record.url as String, record)
        }
        records = byUrl.values().toList().sort { it.objectID as String }
        def output = this.outputFile.get().asFile.tap { it.parentFile.mkdirs() }
        int count = writeMergedRecords(output, records, externalRecords.get().asFile, versionRanks)
        logger.lifecycle("Exported {} Algolia record(s) to {}.", count, output)
    }

    private static int writeMergedRecords(
            File outputFile,
            List<Map<String, Object>> localRecords,
            File external,
            Map<String, Integer> versionRanks
    ) {
        def temporaryFile = new File(outputFile.parentFile, "${outputFile.name}.tmp")
        def urls = new HashSet<String>()
        def localUrls = localRecords*.url as Set<String>
        int[] count = [0]
        try {
            temporaryFile.withWriter('UTF-8') { writer ->
                writer << '[\n'
                int localIndex = 0
                if (external.isFile()) {
                    streamJsonArray(external) { Map<String, Object> record ->
                        if (!record.objectID || !record.title || !record.content || localUrls.contains(record.url as String)) {
                            return
                        }
                        decorateRecord(record, versionRanks)
                        while (localIndex < localRecords.size() && (localRecords[localIndex].objectID as String) < (record.objectID as String)) {
                            writeRecord(writer, localRecords[localIndex++], urls, count)
                        }
                        if (localIndex < localRecords.size() && (localRecords[localIndex].objectID as String) == (record.objectID as String)) {
                            writeRecord(writer, localRecords[localIndex++], urls, count)
                        } else {
                            writeRecord(writer, record, urls, count)
                        }
                    }
                }
                while (localIndex < localRecords.size()) {
                    writeRecord(writer, localRecords[localIndex++], urls, count)
                }
                writer << '\n]\n'
            }
            if (outputFile.exists() && !outputFile.delete()) {
                throw new IOException("Unable to replace ${outputFile}")
            }
            if (!temporaryFile.renameTo(outputFile)) {
                throw new IOException("Unable to move ${temporaryFile} to ${outputFile}")
            }
        } finally {
            if (temporaryFile.exists()) temporaryFile.delete()
        }
        count[0]
    }

    private static void writeRecord(
            Writer writer,
            Map<String, Object> record,
            Set<String> urls,
            int[] count
    ) {
        def url = record.url as String
        if (!url || !urls.add(url)) return
        if (count[0] > 0) writer << ',\n'
        writer << JsonOutput.toJson(record)
        count[0]++
    }

    private static void decorateRecord(Map<String, Object> record, Map<String, Integer> versionRanks) {
        record.sourceRank = sourceRank(record.source as String)
        record.versionRank = versionRank(record.grailsVersion as String, versionRanks)
        record.sortKey = record.objectID
    }

    private static void streamJsonArray(File input, Closure consumer) {
        input.withReader('UTF-8') { Reader reader ->
            StringBuilder object = null
            int depth = 0
            boolean quoted = false
            boolean escaped = false
            int next
            while ((next = reader.read()) != -1) {
                char character = (char) next
                if (object == null) {
                    if (character == '{' as char) {
                        object = new StringBuilder()
                        object.append(character)
                        depth = 1
                        quoted = false
                        escaped = false
                    }
                    continue
                }
                object.append(character)
                if (escaped) {
                    escaped = false
                    continue
                }
                if (quoted && character == '\\' as char) {
                    escaped = true
                    continue
                }
                if (character == '"' as char) {
                    quoted = !quoted
                    continue
                }
                if (quoted) continue
                if (character == '{' as char) depth++
                if (character == '}' as char) depth--
                if (depth == 0) {
                    Object parsed = new JsonSlurper().parseText(object.toString())
                    if (parsed instanceof Map) consumer.call(parsed as Map<String, Object>)
                    object = null
                }
            }
            if (object != null) throw new IllegalArgumentException("Malformed JSON record array: ${input}")
        }
    }

    static List<Map<String, Object>> recordsFromExternalDocument(
            Document document,
            String url,
            String source,
            String version
    ) {
        if (isRedirectOrNonCanonical(document, url)) return []
        if (source == 'api' && (url.matches('(?i).*/(overview|allclasses|allpackages|package)-frame\\.html$') ||
                url.matches('(?i).*/(index|overview-summary|overview-tree|index-all|deprecated-list|help-doc)\\.html$'))) {
            return []
        }
        def contentType = source == 'api' ? 'api' : 'user-documentation'
        if (source == 'api') {
            return [pageRecord(document, url, source, contentType, version, null)]
        }
        sectionRecords(document, url, source, contentType, version)
    }

    static List<Map<String, Object>> recordsFromLocalDocument(
            Document document,
            String relativePath,
            String url
    ) {
        if (isRedirectOrNonCanonical(document, url)) return []
        if (relativePath == 'plugins.html') return pluginRecords(document, url)
        if (relativePath.startsWith('plugins/')) return []
        if (relativePath.startsWith('guides/')) {
            if (relativePath ==~ 'guides/[^/]+/[^/]+/guide/index\\.html') {
                return sectionRecords(document, url, 'guides', 'guide', versionFromGuidePath(relativePath))
            }
            return [pageRecord(document, url, 'guides', 'guide-catalog', null, null)]
        }
        if (relativePath.startsWith('blog/') && relativePath.endsWith('/index.html')) return []
        if (relativePath.startsWith('blog/tag/')) return []
        if (relativePath == 'index.html' || relativePath.endsWith('/index.html')) {
            if (!relativePath.startsWith('guides/')) return [pageRecord(document, url, 'website', 'page', null, null, null)]
        }
        if (relativePath == 'faq.html') {
            def questions = [] as List<Map<String, Object>>
            document.select('.question').each { question ->
                def title = question.select('h1,h2,h3,h4,h5,h6').first()?.text() ?: question.id()
                def anchor = question.id()
                def content = cleanText(question)
                if (title && content) {
                    questions << pageRecord(document, url + (anchor ? "#${anchor}" : ''),
                            'website', 'faq', null, title, content)
                }
            }
            if (!questions.isEmpty()) return questions
        }
        def contentType = relativePath.startsWith('blog/') ? 'blog' :
                relativePath.startsWith('casestudies/') ? 'case-study' : 'page'
        [pageRecord(document, url, 'website', contentType, null, null, null)]
    }

    private static List<Map<String, Object>> pluginRecords(Document document, String url) {
        def records = [:] as Map<String, Map<String, Object>>
        document.select('li.plugin').each { plugin ->
            def title = plugin.select('.plugin-header .name').text().trim()
            if (!title) return
            def pluginUrl = plugin.select('.plugin-header .name a').first()?.absUrl('href')
            if (!pluginUrl) pluginUrl = url.split('#', 2)[0] + "#${slug(title)}"
            def record = baseRecord(
                    stableId('website', pluginUrl),
                    title,
                    cleanText(plugin),
                    pluginUrl,
                    'website',
                    'plugin',
                    null
            )
            record.hierarchy = [lvl0: 'Grails Plugins', lvl1: title]
            def description = plugin.select('.desc').text().trim()
            if (description) record.description = description
            def tags = plugin.select('.labels .label').collect { label ->
                label.text().replaceFirst(/^#/, '').trim()
            }.findAll { it }
            if (!tags.isEmpty()) record.tags = tags
            def grailsVersion = plugin.select('.grails-compat').first()?.text()?.trim()
            if (grailsVersion) record.grailsVersion = grailsVersion
            records.putIfAbsent(pluginUrl, record)
        }
        records.values().toList().sort { it.objectID as String }
    }

    private static List<Map<String, Object>> sectionRecords(
            Document document,
            String url,
            String source,
            String contentType,
            String version
    ) {
        def rootElement = contentRoot(document, source == 'guides' || source == 'user-documentation')
        if (rootElement == null) {
            return []
        }
        def blocks = blocks(rootElement)
        def records = [] as List<Map<String, Object>>
        def documentTitle = document.select('h1').first()?.text() ?: document.title()
        Block currentBlock = null
        def contentList = [] as List<String>
        def headingPathList = [] as List<Block>
        def currentPathList = [] as List<Block>
        for (def block : blocks) {
            if (block.heading) {
                if (currentBlock != null && !contentList.isEmpty()) {
                    records << sectionRecord(currentBlock, contentList, currentPathList, documentTitle, url,
                            source, contentType, version)
                }
                while (!headingPathList.isEmpty() && headingPathList[-1].level >= block.level) {
                    headingPathList.remove(headingPathList.size() - 1)
                }
                headingPathList << block
                currentBlock = block
                currentPathList = new ArrayList<Block>(headingPathList)
                contentList = []
            } else if (currentBlock != null) {
                contentList << block.text
            }
        }
        if (currentBlock != null && !contentList.isEmpty()) {
            records << sectionRecord(
                    currentBlock,
                    contentList as List<String>,
                    currentPathList,
                    documentTitle,
                    url,
                    source,
                    contentType,
                    version
            )
        }
        if (records.isEmpty()) {
            return [pageRecord(document, url, source, contentType, version, documentTitle, cleanText(rootElement))]
        }
        records
    }

    private static Map<String, Object> sectionRecord(
            Block heading,
            List<String> content,
            List<Block> headingPath,
            String documentTitle,
            String url,
            String source,
            String contentType,
            String version
    ) {
        def anchor = heading.anchor ?: slug(heading.text)
        def sectionUrl = url.split('#', 2)[0] + "#${anchor}"
        def text = ([heading.text] + content).join('\n').trim()
        def title = withoutSectionNumber(heading.text)
        def record = baseRecord(
                stableId(source, sectionUrl),
                title,
                text,
                sectionUrl,
                source,
                contentType,
                version
        )
        record.hierarchy = sectionHierarchy(headingPath, documentTitle ?: heading.text, title)
        record
    }

    private static Map<String, String> sectionHierarchy(
            List<Block> headingPathList,
            String documentTitle,
            String currentTitle
    ) {
        def hierarchy = new LinkedHashMap<String, String>()
        hierarchy.lvl0 = withoutSectionNumber(documentTitle)
        headingPathList.each { Block item ->
            if (item.level > 1) {
                hierarchy["lvl${item.level - 1}".toString()] = withoutSectionNumber(item.text)
            }
        }
        if (headingPathList.isEmpty() || headingPathList[-1].level == 1) {
            hierarchy.lvl1 = currentTitle
        }
        hierarchy
    }

    private static Map<String, Object> pageRecord(
            Document document,
            String url,
            String source,
            String contentType,
            String version,
            String overrideTitle,
            String overrideContent = null
    ) {
        def title = overrideTitle ?: document.title() ?: document.select('h1').first()?.text()
        def content = overrideContent ?: cleanText(contentRoot(document, false) ?: document.body())
        def record = baseRecord(
                stableId(source, url),
                title,
                content,
                url,
                source,
                contentType,
                version
        )
        def description = document.select('meta[name=description]').attr('content')
        if (description) record.description = description
        def dateStr = document.select('meta[name=date]').attr('content')
        if (dateStr) record.date = dateStr
        record
    }

    private static Map<String, Object> baseRecord(
            String objectId,
            String title,
            String content,
            String url,
            String source,
            String contentType,
            String version
    ) {
        [
            objectID: objectId,
            title: truncate(title?.trim() ?: 'Untitled', 500),
            content: truncate(content?.trim() ?: '', 9000),
            url: url,
            source: source,
            contentType: contentType,
            sourceRank: sourceRank(source),
            description: '',
            grailsVersion: version ?: '',
            versionRank: 0,
            tags: [],
            date: '',
            hierarchy: [lvl0: title],
            sortKey: objectId
        ]
    }

    private static Element contentRoot(Document document, boolean guide) {
        def rootElement = document.select('#main').first()
        if (rootElement == null) {
            rootElement = guide ?
                    document.select('article.guide').first() :
                    document.select('article.post').first()
        }
        if (rootElement == null) rootElement = document.select('body').first()
        if (rootElement == null) return null
        def copy = rootElement.clone()
        copy.select(
                'script,style,noscript,nav,header,footer,aside,form,#navigation,#table-of-content,.local,.chapter-navigation,.toc-item'
        ).remove()
        copy
    }

    private static String cleanText(Element element) {
        element.text().replaceAll(/\s+/, ' ').trim()
    }

    private static List<Block> blocks(Node node) {
        def result = [] as List<Block>
        if (!(node instanceof Element)) return result
        def element = node as Element
        if (element.tagName().matches('h[1-6]')) {
            result << new Block(true, element.tagName().substring(1) as Integer,
                    cleanText(element), element.id())
            return result
        }
        if (['p', 'li', 'pre', 'blockquote', 'dt', 'dd', 'td'].contains(element.tagName())) {
            def text = cleanText(element)
            if (text) result << new Block(false, 0, text, null)
            return result
        }
        element.children().each { Element child -> result.addAll(blocks(child)) }
        result
    }

    private static boolean isRedirectOrNonCanonical(Document document, String url) {
        if (document.select('frameset').size() > 0) return true
        if (document.select('meta[http-equiv]').any { meta ->
            meta.attr('http-equiv').equalsIgnoreCase('refresh')
        }) return true
        def robots = document.select('meta[name=robots]').attr('content')
        if (robots.toLowerCase().contains('noindex')) return true
        def canonical = document.select('link[rel=canonical]').attr('abs:href')
        canonical && pathOnly(canonical) != pathOnly(url)
    }

    private static String pathOnly(String value) {
        try {
            return new URI(value).toURL().path
        } catch (Exception ignored) {
            return value.split('#', 2)[0]
        }
    }

    private static String versionFromGuidePath(String relativePath) {
        def parts = relativePath.split('/')
        parts.length > 2 ? parts[2] : null
    }

    private static void enrichGuideRecords(
            List<Map<String, Object>> records,
            String relativePath,
            Map<String, Map<String, Object>> guideMetadata
    ) {
        def parts = relativePath.split('/')
        def metadata = guideMetadata["${parts[1]}:${parts[2]}".toString()]
        if (metadata == null) return
        records.each { Map<String, Object> record ->
            if (metadata.title) record.guideTitle = metadata.title
            if (metadata.category) record.category = metadata.category
            if (metadata.tags) record.tags = metadata.tags
        }
    }

    @CompileDynamic
    private static Map<String, Map<String, Object>> loadGuideMetadata(File guidesYml) {
        def root = guidesYml.withReader('UTF-8') { reader -> new Yaml().load(reader) as Map }
        def result = [:] as Map<String, Map<String, Object>>
        ((root?.guides ?: []) as List).each { Map guide ->
            def name = guide.name as String
            def versions = (guide.versions ?: [:]) as Map
            versions.each { versionKey, Object versionValue ->
                if (!(versionValue instanceof Map)) return
                def version = versionValue as Map
                def tags = ((version.tags ?: guide.tags ?: []) as List).collect { it.toString() }
                result["${name}:${versionKey}".toString()] = [
                        title: guide.title as String,
                        category: guide.category as String,
                        tags: tags
                ]
            }
        }
        result
    }

    private static String slug(String value) {
        value.toLowerCase()
                .replaceAll(/[^a-z0-9]+/, '-')
                .replaceAll(/^-|-$/, '')
    }

    private static String withoutSectionNumber(String value) {
        value?.replaceFirst(/^\s*\d+(?:\.\d+)*[.)]?\s+/, '') ?: value
    }

    private static String stableId(String source, String value) {
        def key = "${source}:${value}"
        def digestBytes = MessageDigest.getInstance('SHA-256').digest(key.getBytes('UTF-8'))
        digestBytes.encodeHex().toString().substring(0, 32)
    }

    private static Map<String, Integer> loadVersionRanks(File releases) {
        if (!releases.isFile()) return [:]
        def result = [:] as Map<String, Integer>
        SiteMap.versions(releases).eachWithIndex { version, index ->
            result[version.versionText] = index + 1
        }
        result
    }

    private static int versionRank(String version, Map<String, Integer> ranks) {
        version ? (ranks[version] ?: 0) : 0
    }

    private static int sourceRank(String source) {
        source == 'api' ? 0 : 1
    }

    private static String truncate(String value, int max) {
        value && value.length() > max ? value.substring(0, max) : value
    }

    @CompileStatic
    private static class Block {

        final boolean heading
        final int level
        final String text
        final String anchor

        Block(boolean heading, int level, String text, String anchor) {
            this.heading = heading
            this.level = level
            this.text = text
            this.anchor = anchor
        }
    }
}

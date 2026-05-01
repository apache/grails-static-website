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

import groovy.transform.CompileStatic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * Crawls rendered guide HTML looking for missing local files and broken anchors.
 */
@CompileStatic
class CrawlBuiltGuidesTask extends DefaultTask {

    static final String NAME = 'crawlBuiltGuides'
    static final String GROUP = 'migration'

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty guidesDir = project.objects.directoryProperty()

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty siteRootDir = project.objects.directoryProperty()

    @OutputFile
    final RegularFileProperty reportFile = project.objects.fileProperty()

    @Input
    final Property<Boolean> failOnViolation = project.objects.property(Boolean)

    @TaskAction
    void crawl() {
        File guidesRoot = guidesDir.get().asFile
        File siteRoot = siteRootDir.get().asFile
        File report = reportFile.get().asFile

        if (!guidesRoot.isDirectory()) {
            writeCsv(report, [])
            return
        }

        Map<String, GuideSummary> summaries = discoverGuideVersions(guidesRoot)
        Map<File, Document> documentCache = [:]
        Map<File, Set<String>> anchorCache = [:]

        for (GuideSummary summary : summaries.values()) {
            List<File> htmlFiles = collectHtmlFiles(summary.versionDir)
            if (htmlFiles.isEmpty()) {
                summary.issues << 'No rendered HTML files were found for this guide version.'
                continue
            }
            for (File htmlFile : htmlFiles) {
                Document document = parseDocument(htmlFile, documentCache)
                checkLinkElements(summary, htmlFile, document, siteRoot, documentCache, anchorCache)
            }
        }

        List<GuideSummary> results = summaries.values() as List<GuideSummary>
        for (GuideSummary summary : results) {
            summary.status = determineStatus(summary.issues.isEmpty())
        }
        writeCsv(report, results)

        List<GuideSummary> failed = results.findAll { GuideSummary summary -> summary.status == 'FAILED' } as List<GuideSummary>
        if (!failed.isEmpty()) {
            throw new GradleException("Broken rendered guide references detected. See ${report.absolutePath}.")
        }
    }

    static TaskProvider<CrawlBuiltGuidesTask> register(Project project) {
        project.tasks.register(NAME, CrawlBuiltGuidesTask) { CrawlBuiltGuidesTask task ->
            task.group = GROUP
            task.description = 'Smoke-crawls rendered guide HTML for broken local links, assets, and anchors.'
            task.guidesDir.convention(project.layout.buildDirectory.dir('dist/guides'))
            task.siteRootDir.convention(project.layout.buildDirectory.dir('dist'))
            task.reportFile.convention(project.layout.buildDirectory.file('reports/crawl-built-guides.csv'))
            task.failOnViolation.convention(isHardFailMode(project))
            task.dependsOn('buildAllGuides')
        }
    }

    private void checkLinkElements(GuideSummary summary, File htmlFile, Document document, File siteRoot,
            Map<File, Document> documentCache, Map<File, Set<String>> anchorCache) {
        checkElements(summary, htmlFile, document, siteRoot, documentCache, anchorCache, 'a[href]', 'href', true)
        checkElements(summary, htmlFile, document, siteRoot, documentCache, anchorCache, 'img[src]', 'src', false)
        checkElements(summary, htmlFile, document, siteRoot, documentCache, anchorCache, 'link[href]', 'href', false)
        checkElements(summary, htmlFile, document, siteRoot, documentCache, anchorCache, 'script[src]', 'src', false)
    }

    private void checkElements(GuideSummary summary, File htmlFile, Document document, File siteRoot,
            Map<File, Document> documentCache, Map<File, Set<String>> anchorCache,
            String selector, String attribute, boolean checkAnchor) {

        for (Element element : document.select(selector)) {
            String reference = element.attr(attribute)?.trim()
            if (shouldIgnore(reference)) {
                continue
            }

            ReferenceParts parts = splitReference(reference)
            if (parts.pathPart.isEmpty()) {
                if (checkAnchor && !parts.fragment.isEmpty()) {
                    ensureAnchor(summary, htmlFile, htmlFile, parts.fragment, documentCache, anchorCache)
                }
                continue
            }

            File target = resolveTarget(htmlFile, siteRoot, parts.pathPart)
            if (!target.isFile()) {
                summary.issues.add("${relativePath(summary.versionDir, htmlFile)} -> missing ${attribute} target ${parts.pathPart}".toString())
                continue
            }
            if (checkAnchor && !parts.fragment.isEmpty()) {
                ensureAnchor(summary, htmlFile, target, parts.fragment, documentCache, anchorCache)
            }
        }
    }

    private void ensureAnchor(GuideSummary summary, File sourceFile, File targetFile, String fragment,
            Map<File, Document> documentCache, Map<File, Set<String>> anchorCache) {
        Set<String> anchors = anchorCache.computeIfAbsent(targetFile) {
            extractAnchors(parseDocument(targetFile, documentCache))
        }
        if (!anchors.contains(fragment)) {
            summary.issues.add("${relativePath(summary.versionDir, sourceFile)} -> missing anchor #${fragment} in ${relativePath(summary.versionDir, targetFile)}".toString())
        }
    }

    private static Map<String, GuideSummary> discoverGuideVersions(File guidesRoot) {
        Map<String, GuideSummary> summaries = [:]
        File[] guideDirs = guidesRoot.listFiles()
        if (guideDirs == null) {
            return summaries
        }
        for (File guideDir : guideDirs) {
            if (!guideDir.isDirectory()) {
                continue
            }
            File[] versionDirs = guideDir.listFiles()
            if (versionDirs == null) {
                continue
            }
            for (File versionDir : versionDirs) {
                if (!versionDir.isDirectory()) {
                    continue
                }
                GuideSummary summary = new GuideSummary(
                        guide: guideDir.name,
                        version: versionDir.name,
                        versionDir: versionDir,
                )
                summaries[keyFor(guideDir.name, versionDir.name)] = summary
            }
        }
        summaries
    }

    private static List<File> collectHtmlFiles(File versionDir) {
        List<File> htmlFiles = []
        versionDir.eachFileRecurse { File candidate ->
            if (candidate.isFile() && candidate.name.endsWith('.html')) {
                htmlFiles << candidate
            }
        }
        htmlFiles.sort { File left, File right -> left.absolutePath <=> right.absolutePath }
        htmlFiles
    }

    private static Document parseDocument(File htmlFile, Map<File, Document> documentCache) {
        documentCache.computeIfAbsent(htmlFile) {
            Jsoup.parse(htmlFile, 'UTF-8')
        }
    }

    private static Set<String> extractAnchors(Document document) {
        Set<String> anchors = [] as LinkedHashSet<String>
        for (Element element : document.select('[id]')) {
            String id = element.attr('id')?.trim()
            if (!id.isEmpty()) {
                anchors << id
            }
        }
        for (Element element : document.select('a[name]')) {
            String name = element.attr('name')?.trim()
            if (!name.isEmpty()) {
                anchors << name
            }
        }
        anchors
    }

    private static File resolveTarget(File htmlFile, File siteRoot, String rawPath) {
        String decoded = URLDecoder.decode(rawPath, StandardCharsets.UTF_8)
        Path candidate = decoded.startsWith('/')
                ? siteRoot.toPath().resolve(decoded.substring(1))
                : htmlFile.parentFile.toPath().resolve(decoded)
        Path normalized = candidate.normalize()
        File target = normalized.toFile()
        if (target.isDirectory()) {
            return normalized.resolve('index.html').toFile()
        }
        if (!target.exists() && !decoded.contains('.') && normalized.resolveSibling(normalized.fileName.toString() + '.html').toFile().isFile()) {
            return normalized.resolveSibling(normalized.fileName.toString() + '.html').toFile()
        }
        if (!target.exists() && normalized.resolve('index.html').toFile().isFile()) {
            return normalized.resolve('index.html').toFile()
        }
        target
    }

    private String determineStatus(boolean clean) {
        if (clean) {
            return 'VERIFIED'
        }
        failOnViolation.get() ? 'FAILED' : 'REVIEW'
    }

    private static ReferenceParts splitReference(String reference) {
        String noQuery = reference
        int queryIndex = noQuery.indexOf('?')
        if (queryIndex >= 0) {
            noQuery = noQuery.substring(0, queryIndex)
        }
        int hashIndex = noQuery.indexOf('#')
        if (hashIndex < 0) {
            return new ReferenceParts(pathPart: noQuery, fragment: '')
        }
        new ReferenceParts(
                pathPart: noQuery.substring(0, hashIndex),
                fragment: noQuery.substring(hashIndex + 1),
        )
    }

    private static boolean shouldIgnore(String reference) {
        if (reference == null) {
            return true
        }
        String value = reference.trim()
        if (value.isEmpty() || value == '#') {
            return true
        }
        String lower = value.toLowerCase(Locale.ENGLISH)
        lower.startsWith('http://') ||
                lower.startsWith('https://') ||
                lower.startsWith('mailto:') ||
                lower.startsWith('tel:') ||
                lower.startsWith('javascript:') ||
                lower.startsWith('data:') ||
                lower.startsWith('blob:') ||
                lower.startsWith('//')
    }

    private static void writeCsv(File outputFile, List<GuideSummary> results) {
        outputFile.parentFile.mkdirs()
        StringBuilder sb = new StringBuilder('guide,version,status,issueCount,details\n')
        for (GuideSummary result : results.sort { GuideSummary left, GuideSummary right ->
            int guideCompare = left.guide <=> right.guide
            guideCompare != 0 ? guideCompare : (left.version <=> right.version)
        }) {
            List<String> issues = result.issues as List<String>
            String details = issues.isEmpty()
                    ? 'No broken local references or anchors detected.'
                    : abbreviate(issues.join(' | '))
            sb << sanitize(result.guide) << ','
            sb << sanitize(result.version) << ','
            sb << sanitize(result.status ?: 'VERIFIED') << ','
            sb << issues.size() << ','
            sb << sanitize(details) << '\n'
        }
        outputFile.text = sb.toString()
    }

    private static String keyFor(String guide, String version) {
        guide + '|' + version
    }

    private static String relativePath(File root, File file) {
        root.toPath().relativize(file.toPath()).toString().replace('\\', '/')
    }

    private static boolean isHardFailMode(Project project) {
        String mode = (project.findProperty('verificationMode') ?: '') as String
        mode.equalsIgnoreCase('hard-fail')
    }

    private static String abbreviate(String value) {
        if (value.size() <= 1200) {
            return value
        }
        value.substring(0, 1197) + '...'
    }

    private static String sanitize(String value) {
        (value ?: '')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace(',', ';')
                .trim()
    }

    private static final class ReferenceParts {
        String pathPart
        String fragment
    }

    private static final class GuideSummary {
        String guide
        String version
        String status
        File versionDir
        Set<String> issues = [] as LinkedHashSet<String>
    }
}

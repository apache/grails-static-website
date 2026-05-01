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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import org.yaml.snakeyaml.Yaml

/**
 * Scans every {@code *.html} file under {@link #scanDir} for external
 * https:// references in {@code <script src>}, {@code <link href>},
 * {@code <iframe src>}, and {@code <img src>} tags, and emits a
 * violation report for any host not listed in {@code conf/csp-allowlist.yml}.
 *
 * <p>The intent is to catch resources loaded from third-party origins
 * before they become Content Security Policy violations on the live site.</p>
 *
 * <p>By default, presence of any unknown-host reference fails the build.
 * Use {@code -PcspFailOnViolation=false} to demote to warnings while
 * iterating.</p>
 */
@CompileStatic
class CspScanTask extends DefaultTask {

    static final String NAME = 'cspScan'
    static final String GROUP = 'migration'

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty scanDir = project.objects.directoryProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty allowlistFile = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty reportFile = project.objects.fileProperty()

    @Input
    final Property<Boolean> failOnViolation = project.objects.property(Boolean).convention(true)

    @TaskAction
    void scan() {
        File rootDir = scanDir.get().asFile
        File listFile = allowlistFile.get().asFile
        File report = reportFile.get().asFile

        if (!rootDir.isDirectory()) {
            // Nothing rendered yet -- emit empty report rather than failing
            report.parentFile.mkdirs()
            report.text = "CSP scan skipped: ${rootDir} does not exist (run renderGuide tasks first).\n"
            return
        }

        Set<String> allowed = parseAllowlist(listFile)
        List<String> scannedTags = parseScannedTags(listFile)

        Map<String, Map<String, Set<String>>> violations = [:]   // host -> file -> set of urls
        int htmlFilesScanned = 0

        rootDir.eachFileRecurse { File f ->
            if (f.isFile() && f.name.endsWith('.html')) {
                htmlFilesScanned++
                Document doc = Jsoup.parse(f, 'UTF-8')
                for (String tag : scannedTags) {
                    String attr = (tag in ['script', 'iframe', 'img']) ? 'src' : 'href'
                    for (Element el : doc.select(tag)) {
                        String url = el.attr(attr)?.trim()
                        if (url && url.startsWith('https://')) {
                            String host = hostOf(url)
                            if (host && !allowed.contains(host)) {
                                String relPath = rootDir.toPath().relativize(f.toPath()).toString()
                                violations.computeIfAbsent(host) { [:] }
                                          .computeIfAbsent(relPath) { [] as Set }
                                          .add(url)
                            }
                        }
                    }
                }
            }
        }

        report.parentFile.mkdirs()
        report.text = renderReport(htmlFilesScanned, allowed, violations)

        if (!violations.isEmpty()) {
            int totalRefs = 0
            for (Map<String, Set<String>> filesByHost : violations.values()) {
                for (Set<String> urls : filesByHost.values()) {
                    totalRefs += urls.size()
                }
            }
            String summary = "${violations.size()} non-allowlisted host(s) referenced from ${htmlFilesScanned} HTML file(s) (${totalRefs} total reference(s))."
            logger.warn("CSP scan: ${summary}")
            logger.warn("Report: ${report.absolutePath}")
            if (failOnViolation.get()) {
                throw new GradleException("CSP scan FAILED: ${summary} See ${report.absolutePath}. " +
                        'Re-run with -PcspFailOnViolation=false to demote to warnings while iterating.')
            }
        } else {
            logger.lifecycle("CSP scan: ${htmlFilesScanned} HTML file(s) clean (only allowlisted hosts referenced).")
        }
    }

    @CompileDynamic
    private static Set<String> parseAllowlist(File yaml) {
        Map root = yaml.withReader('UTF-8') { reader -> new Yaml().load(reader) as Map }
        List entries = (root.allowedHosts ?: []) as List
        Set<String> result = [] as LinkedHashSet
        for (Object e : entries) {
            if (e instanceof Map) {
                String host = (e.host ?: e.hostname ?: '') as String
                if (host) result.add(host.toLowerCase())
            } else if (e instanceof String) {
                result.add((e as String).toLowerCase())
            }
        }
        result
    }

    @CompileDynamic
    private static List<String> parseScannedTags(File yaml) {
        Map root = yaml.withReader('UTF-8') { reader -> new Yaml().load(reader) as Map }
        List tags = (root.scannedTags ?: ['script', 'link', 'iframe', 'img']) as List
        tags.collect { it.toString().toLowerCase() }
    }

    private static String hostOf(String url) {
        try {
            return new URI(url).host?.toLowerCase()
        } catch (Exception ignored) {
            return null
        }
    }

    private static String renderReport(int htmlFilesScanned, Set<String> allowed,
            Map<String, Map<String, Set<String>>> violations) {
        StringBuilder sb = new StringBuilder()
        sb << '# CSP Scan Report\n\n'
        sb << "HTML files scanned: ${htmlFilesScanned}\n"
        sb << "Allowed hosts: ${allowed.size()}\n"
        sb << "Non-allowlisted hosts found: ${violations.size()}\n\n"

        if (violations.isEmpty()) {
            sb << '## Result: CLEAN\n\nNo non-allowlisted external resources referenced.\n'
            return sb.toString()
        }

        sb << '## Violations\n\n'
        for (String host : violations.keySet().sort()) {
            sb << "### `${host}`\n\n"
            sb << 'To allowlist this host: add an entry to `conf/csp-allowlist.yml` with a one-line justification.\n\n'
            Map<String, Set<String>> filesByHost = violations[host]
            for (String file : filesByHost.keySet().sort()) {
                sb << "- `${file}`\n"
                for (String url : filesByHost[file].sort()) {
                    sb << "    - ${url}\n"
                }
            }
            sb << '\n'
        }
        sb.toString()
    }

    static void register(Project project) {
        project.tasks.register(NAME, CspScanTask) { CspScanTask task ->
            task.group = GROUP
            task.description = 'Scans build/dist/ for non-allowlisted https:// references in <script>, <link>, <iframe>, <img> tags.'
            task.scanDir.set(project.layout.buildDirectory.dir('dist'))
            task.allowlistFile.set(
                    project.rootProject.layout.projectDirectory.file('conf/csp-allowlist.yml'))
            task.reportFile.set(project.layout.buildDirectory.file('reports/csp-scan.md'))
            task.failOnViolation.convention(isHardFailMode(project))
            task.dependsOn('buildAllGuides')
            if (project.hasProperty('cspFailOnViolation')) {
                task.failOnViolation.set(Boolean.parseBoolean(project.property('cspFailOnViolation') as String))
            }
        }
    }

    private static boolean isHardFailMode(Project project) {
        String mode = (project.findProperty('verificationMode') ?: '') as String
        mode.equalsIgnoreCase('hard-fail')
    }
}

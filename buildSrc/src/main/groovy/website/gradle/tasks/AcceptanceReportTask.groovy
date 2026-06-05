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
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * Aggregates per-guide verification gate outputs into build/reports/acceptance.csv.
 */
@CompileStatic
class AcceptanceReportTask extends DefaultTask {

    static final String NAME = 'acceptanceReport'
    static final String GROUP = 'migration'

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty guidesDir = project.objects.directoryProperty()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty asciidoctorReportFile = project.objects.fileProperty()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty crawlReportFile = project.objects.fileProperty()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty cspReportFile = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty reportFile = project.objects.fileProperty()

    @Input
    final Property<Boolean> failOnViolation = project.objects.property(Boolean)

    @TaskAction
    void report() {
        File guidesRoot = guidesDir.get().asFile
        Map<String, GateResult> asciidoctorResults = parseCsvReport(optionalFile(asciidoctorReportFile))
        Map<String, GateResult> crawlResults = parseCsvReport(optionalFile(crawlReportFile))
        CspAggregation cspAggregation = parseCspReport(optionalFile(cspReportFile))

        Set<String> guideKeys = [] as LinkedHashSet<String>
        guideKeys.addAll(discoverGuideKeys(guidesRoot))
        guideKeys.addAll(asciidoctorResults.keySet())
        guideKeys.addAll(crawlResults.keySet())
        guideKeys.addAll(cspAggregation.issuesByGuide.keySet())

        if (guideKeys.isEmpty()) {
            writeCsv(reportFile.get().asFile, [])
            return
        }

        List<AcceptanceRow> rows = []
        for (String key : guideKeys.toList().sort()) {
            Pair pair = splitKey(key)
            GateResult asciidoctor = asciidoctorResults[key] ?: GateResult.review('asciidoctorWarningGate report row missing.')
            GateResult crawl = crawlResults[key] ?: GateResult.review('crawlBuiltGuides report row missing.')
            GateResult csp = cspResultFor(key, cspAggregation)
            String verdict = mergeVerdict([asciidoctor.status, crawl.status, csp.status])
            String details = summarizeDetails(asciidoctor, crawl, csp)
            rows << new AcceptanceRow(
                    guide: pair.guide,
                    version: pair.version,
                    asciidoctorWarningGate: asciidoctor.status,
                    crawlBuiltGuides: crawl.status,
                    cspScan: csp.status,
                    verdict: verdict,
                    details: details,
            )
        }

        File outputFile = reportFile.get().asFile
        writeCsv(outputFile, rows)

        boolean hasFailed = rows.any { AcceptanceRow row -> row.verdict == 'FAILED' }
        if (hasFailed && failOnViolation.get()) {
            throw new GradleException("Acceptance report contains FAILED rows. See ${outputFile.absolutePath}.")
        }
    }

    static TaskProvider<AcceptanceReportTask> register(Project project) {
        project.tasks.register(NAME, AcceptanceReportTask) { AcceptanceReportTask task ->
            task.group = GROUP
            task.description = 'Aggregates guide verification outputs into build/reports/acceptance.csv.'
            task.guidesDir.convention(project.layout.buildDirectory.dir('dist/guides'))
            task.asciidoctorReportFile.convention(project.layout.buildDirectory.file('reports/asciidoctor-warning-gate.csv'))
            task.crawlReportFile.convention(project.layout.buildDirectory.file('reports/crawl-built-guides.csv'))
            task.cspReportFile.convention(project.layout.buildDirectory.file('reports/csp-scan.md'))
            task.reportFile.convention(project.layout.buildDirectory.file('reports/acceptance.csv'))
            task.failOnViolation.convention(isHardFailMode(project))
            task.dependsOn(AsciidoctorWarningGateTask.NAME)
            task.dependsOn(CrawlBuiltGuidesTask.NAME)
            task.dependsOn(CspScanTask.NAME)
        }
    }

    private static Set<String> discoverGuideKeys(File guidesRoot) {
        Set<String> keys = [] as LinkedHashSet<String>
        if (!guidesRoot.isDirectory()) {
            return keys
        }
        File[] guideDirs = guidesRoot.listFiles()
        if (guideDirs == null) {
            return keys
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
                if (versionDir.isDirectory()) {
                    keys << keyFor(guideDir.name, versionDir.name)
                }
            }
        }
        keys
    }

    private static Map<String, GateResult> parseCsvReport(File csvFile) {
        Map<String, GateResult> results = [:]
        if (csvFile == null || !csvFile.isFile()) {
            return results
        }
        List<String> lines = csvFile.readLines('UTF-8')
        for (int index = 1; index < lines.size(); index++) {
            String line = lines[index]
            if (line.trim().isEmpty()) {
                continue
            }
            List<String> parts = splitCsv(line)
            if (parts.size() < 5) {
                continue
            }
            String key = keyFor(parts[0], parts[1])
            results[key] = new GateResult(
                    status: parts[2],
                    details: parts[4],
            )
        }
        results
    }

    private static CspAggregation parseCspReport(File reportFile) {
        CspAggregation aggregation = new CspAggregation(clean: true)
        if (reportFile == null || !reportFile.isFile()) {
            aggregation.clean = false
            aggregation.generalMessage = 'cspScan report is missing.'
            return aggregation
        }
        reportFile.eachLine('UTF-8') { String line ->
            String trimmed = line.trim()
            if (trimmed.startsWith('- `guides/')) {
                String path = trimmed.substring(3, trimmed.length() - 1)
                List<String> segments = path.replace('\\', '/').tokenize('/')
                if (segments.size() >= 4) {
                    String key = keyFor(segments[1], segments[2])
                    aggregation.issuesByGuide.computeIfAbsent(key) { [] }.add(path)
                    aggregation.clean = false
                }
            } else if (trimmed.startsWith('- `guides\\')) {
                String path = trimmed.substring(3, trimmed.length() - 1)
                List<String> segments = path.replace('\\', '/').tokenize('/')
                if (segments.size() >= 4) {
                    String key = keyFor(segments[1], segments[2])
                    aggregation.issuesByGuide.computeIfAbsent(key) { [] }.add(path)
                    aggregation.clean = false
                }
            }
            if (trimmed.startsWith('Non-allowlisted hosts found:')) {
                String count = trimmed.substring(trimmed.lastIndexOf(':') + 1).trim()
                if (count != '0') {
                    aggregation.clean = false
                }
            }
            if (trimmed.contains('Result: CLEAN')) {
                aggregation.clean = true
            }
        }
        aggregation
    }

    private GateResult cspResultFor(String key, CspAggregation aggregation) {
        if (!aggregation.issuesByGuide.containsKey(key)) {
            if (aggregation.clean) {
                return GateResult.verified('No non-allowlisted external hosts detected for this guide version.')
            }
            if (aggregation.generalMessage != null) {
                return GateResult.review(aggregation.generalMessage)
            }
            return GateResult.verified('No non-allowlisted external hosts detected for this guide version.')
        }
        String details = abbreviate(aggregation.issuesByGuide[key].join(' | '))
        return failOnViolation.get()
                ? GateResult.failed(details)
                : GateResult.review(details)
    }

    private static List<String> splitCsv(String line) {
        List<String> parts = []
        StringBuilder current = new StringBuilder()
        int commasSeen = 0
        for (int index = 0; index < line.length(); index++) {
            char ch = line.charAt(index)
            if (ch == ',' && commasSeen < 4) {
                parts << current.toString()
                current.setLength(0)
                commasSeen++
            } else {
                current.append(ch)
            }
        }
        parts << current.toString()
        parts
    }

    private static String mergeVerdict(List<String> statuses) {
        if (statuses.contains('FAILED')) {
            return 'FAILED'
        }
        if (statuses.contains('REVIEW')) {
            return 'REVIEW'
        }
        'VERIFIED'
    }

    private static String summarizeDetails(GateResult asciidoctor, GateResult crawl, GateResult csp) {
        List<String> details = []
        addDetail(details, 'asciidoctorWarningGate', asciidoctor)
        addDetail(details, 'crawlBuiltGuides', crawl)
        addDetail(details, 'cspScan', csp)
        if (details.isEmpty()) {
            return 'All guide verification gates passed.'
        }
        abbreviate(details.join(' | '))
    }

    private static void addDetail(List<String> details, String gate, GateResult result) {
        if (result.status != 'VERIFIED' && result.details != null && !result.details.isEmpty()) {
            details.add("${gate}: ${result.details}".toString())
        }
    }

    private static void writeCsv(File outputFile, List<AcceptanceRow> rows) {
        outputFile.parentFile.mkdirs()
        StringBuilder sb = new StringBuilder('guide,version,asciidoctorWarningGate,crawlBuiltGuides,cspScan,verdict,details\n')
        for (AcceptanceRow row : rows) {
            sb << sanitize(row.guide) << ','
            sb << sanitize(row.version) << ','
            sb << sanitize(row.asciidoctorWarningGate) << ','
            sb << sanitize(row.crawlBuiltGuides) << ','
            sb << sanitize(row.cspScan) << ','
            sb << sanitize(row.verdict) << ','
            sb << sanitize(row.details) << '\n'
        }
        outputFile.text = sb.toString()
    }

    private static File optionalFile(RegularFileProperty property) {
        property.isPresent() ? property.get().asFile : null
    }

    private static boolean isHardFailMode(Project project) {
        String mode = (project.findProperty('verificationMode') ?: '') as String
        mode.equalsIgnoreCase('hard-fail')
    }

    private static String keyFor(String guide, String version) {
        guide + '|' + version
    }

    private static Pair splitKey(String key) {
        int separator = key.indexOf('|')
        new Pair(
                guide: separator >= 0 ? key.substring(0, separator) : key,
                version: separator >= 0 ? key.substring(separator + 1) : '',
        )
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

    private static final class Pair {
        String guide
        String version
    }

    private static final class GateResult {
        String status
        String details

        static GateResult verified(String details) {
            new GateResult(status: 'VERIFIED', details: details)
        }

        static GateResult review(String details) {
            new GateResult(status: 'REVIEW', details: details)
        }

        static GateResult failed(String details) {
            new GateResult(status: 'FAILED', details: details)
        }
    }

    private static final class CspAggregation {
        boolean clean = true
        String generalMessage
        Map<String, List<String>> issuesByGuide = [:].withDefault { [] }
    }

    private static final class AcceptanceRow {
        String guide
        String version
        String asciidoctorWarningGate
        String crawlBuiltGuides
        String cspScan
        String verdict
        String details
    }
}

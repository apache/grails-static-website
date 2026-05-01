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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import website.qa.AdHocFixtureDiff

/**
 * Extracts structural fingerprints from rendered guides and compares them to a
 * baseline when one is available.
 */
@CompileStatic
class StructuralDiffGuidesTask extends DefaultTask {

    static final String NAME = 'structuralDiffGuides'
    static final String GROUP = 'migration'

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty guidesDir = project.objects.directoryProperty()

    @Input
    final Property<String> baselineDirPath = project.objects.property(String)

    @OutputDirectory
    final DirectoryProperty fingerprintDir = project.objects.directoryProperty()

    @OutputFile
    final RegularFileProperty reportFile = project.objects.fileProperty()

    @Input
    final Property<Boolean> failOnViolation = project.objects.property(Boolean)

    @TaskAction
    void diff() {
        File guidesRoot = guidesDir.get().asFile
        File baselinesRoot = new File(baselineDirPath.get())
        File fingerprintsRoot = fingerprintDir.get().asFile
        File report = reportFile.get().asFile

        if (!guidesRoot.isDirectory()) {
            writeCsv(report, [])
            return
        }

        List<GuideSummary> results = []
        for (File guideDir : sortedDirectories(guidesRoot)) {
            for (File versionDir : sortedDirectories(guideDir)) {
                GuideSummary summary = inspectGuideVersion(guideDir.name, versionDir, baselinesRoot, fingerprintsRoot)
                summary.status = determineStatus(summary)
                results << summary
            }
        }

        writeCsv(report, results)

        List<GuideSummary> failed = results.findAll { GuideSummary summary -> summary.status == 'FAILED' } as List<GuideSummary>
        if (!failed.isEmpty()) {
            throw new GradleException("Structural guide differences detected. See ${report.absolutePath}.")
        }
    }

    static TaskProvider<StructuralDiffGuidesTask> register(Project project) {
        project.tasks.register(NAME, StructuralDiffGuidesTask) { StructuralDiffGuidesTask task ->
            task.group = GROUP
            task.description = 'Extracts structural fingerprints from rendered guides and compares them with available baselines.'
            task.guidesDir.convention(project.layout.buildDirectory.dir('dist/guides'))
            task.baselineDirPath.convention(project.rootProject.layout.projectDirectory.dir('buildSrc/src/test/resources/structural-baseline').asFile.absolutePath)
            task.fingerprintDir.convention(project.layout.buildDirectory.dir('reports/structural-fingerprints'))
            task.reportFile.convention(project.layout.buildDirectory.file('reports/structural-diff-guides.csv'))
            task.failOnViolation.convention(isHardFailMode(project))
            task.dependsOn('buildAllGuides')
        }
    }

    private GuideSummary inspectGuideVersion(String guideName, File versionDir, File baselinesRoot, File fingerprintsRoot) {
        GuideSummary summary = new GuideSummary(
                guide: guideName,
                version: versionDir.name,
        )

        File renderedFile = locateRenderedFile(versionDir)
        if (renderedFile == null) {
            summary.differences << 'No guide/single.html, guide/index.html, or index.html was found for this rendered guide version.'
            return summary
        }

        Fingerprint localFingerprint = fingerprint(renderedFile, versionDir)
        writeFingerprint(new File(fingerprintsRoot, "${guideName}/${versionDir.name}.json"), localFingerprint)

        File baselineFile = resolveBaselineFile(baselinesRoot, guideName, versionDir.name)
        Fingerprint baselineFingerprint = baselineFile == null
                ? localFingerprint
                : fingerprint(baselineFile, baselineFile.parentFile)
        boolean selfBaseline = baselineFile == null
        summary.selfBaseline = selfBaseline

        if (localFingerprint.headingTree.isEmpty()) {
            summary.differences.add("${localFingerprint.sourcePath} contains no headings.".toString())
        }
        if (localFingerprint.totalAnchors == 0) {
            summary.differences.add("${localFingerprint.sourcePath} contains no anchor IDs.".toString())
        }

        if (!selfBaseline) {
            summary.differences.addAll(compare(localFingerprint, baselineFingerprint))
        }

        if (selfBaseline && summary.differences.isEmpty()) {
            summary.differencesInfo = 'Fingerprint extracted successfully. Baseline comparison is currently self-referential until production snapshots are checked in.'
        } else if (selfBaseline) {
            summary.differencesInfo = 'Fingerprint extracted successfully, but the rendered file itself contains structural issues.'
        } else {
            summary.differencesInfo = "Compared ${localFingerprint.sourcePath} to ${baselineFingerprint.sourcePath}."
        }
        summary
    }

    private String determineStatus(GuideSummary summary) {
        if (!summary.differences.isEmpty()) {
            return failOnViolation.get() ? 'FAILED' : 'REVIEW'
        }
        summary.selfBaseline ? 'REVIEW' : 'VERIFIED'
    }

    private static List<String> compare(Fingerprint localFingerprint, Fingerprint baselineFingerprint) {
        List<String> differences = []
        if (localFingerprint.headingTree != baselineFingerprint.headingTree) {
            differences.add("Heading tree mismatch: local=${localFingerprint.headingTree.size()} baseline=${baselineFingerprint.headingTree.size()}".toString())
        }
        Set<String> missingAnchors = baselineFingerprint.anchorIds - localFingerprint.anchorIds
        if (!missingAnchors.isEmpty()) {
            differences.add("${missingAnchors.size()} anchor IDs are missing from the rendered output.".toString())
        }
        if (localFingerprint.totalHeadings != baselineFingerprint.totalHeadings) {
            differences.add("Heading count drift: local=${localFingerprint.totalHeadings} baseline=${baselineFingerprint.totalHeadings}".toString())
        }
        if (localFingerprint.sectionCount != baselineFingerprint.sectionCount) {
            differences.add("Section count drift: local=${localFingerprint.sectionCount} baseline=${baselineFingerprint.sectionCount}".toString())
        }
        if (localFingerprint.codeBlockCount != baselineFingerprint.codeBlockCount) {
            differences.add("Code block count drift: local=${localFingerprint.codeBlockCount} baseline=${baselineFingerprint.codeBlockCount}".toString())
        }
        if (localFingerprint.imageCount != baselineFingerprint.imageCount) {
            differences.add("Image count drift: local=${localFingerprint.imageCount} baseline=${baselineFingerprint.imageCount}".toString())
        }
        differences
    }

    private static Fingerprint fingerprint(File htmlFile, File versionRoot) {
        Document document = Jsoup.parse(htmlFile, 'UTF-8')
        AdHocFixtureDiff.Metrics metrics = AdHocFixtureDiff.measure(document)
        List<String> headingTree = []
        for (Element element : document.select('h1, h2, h3, h4, h5, h6')) {
            String text = normalizeText(element.text())
            if (!text.isEmpty()) {
                headingTree.add("${element.tagName()}:${text}".toString())
            }
        }

        new Fingerprint(
                sourcePath: versionRoot.toPath().relativize(htmlFile.toPath()).toString().replace('\\', '/'),
                totalHeadings: metrics.totalHeadings,
                sectionCount: metrics.sections,
                totalAnchors: metrics.anchors,
                codeBlockCount: metrics.codeBlocks,
                imageCount: metrics.images,
                headingTree: headingTree,
                anchorIds: new LinkedHashSet<String>(metrics.anchorIds),
        )
    }

    private static void writeFingerprint(File outputFile, Fingerprint fingerprint) {
        outputFile.parentFile.mkdirs()
        Map<String, Object> payload = [
                sourcePath     : fingerprint.sourcePath,
                totalHeadings  : fingerprint.totalHeadings,
                sectionCount   : fingerprint.sectionCount,
                totalAnchors   : fingerprint.totalAnchors,
                codeBlockCount : fingerprint.codeBlockCount,
                imageCount     : fingerprint.imageCount,
                headingTree    : fingerprint.headingTree,
                anchorIds      : fingerprint.anchorIds as List<String>,
        ]
        outputFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(payload)) + '\n'
    }

    private static File locateRenderedFile(File versionDir) {
        List<String> candidates = ['guide/single.html', 'guide/index.html', 'index.html']
        for (String candidate : candidates) {
            File renderedFile = new File(versionDir, candidate)
            if (renderedFile.isFile()) {
                return renderedFile
            }
        }
        null
    }

    private static File resolveBaselineFile(File baselinesRoot, String guideName, String version) {
        // TODO: Integrate captured production baseline snapshots from https://guides.grails.org/<guide>/<version>/ once they are committed.
        if (!baselinesRoot.isDirectory()) {
            return null
        }
        List<String> candidates = [
                "${guideName}/${version}/guide/single.html".toString(),
                "${guideName}/${version}/guide/index.html".toString(),
                "${guideName}/${version}/index.html".toString(),
        ]
        for (String candidate : candidates) {
            File baselineFile = new File(baselinesRoot, candidate)
            if (baselineFile.isFile()) {
                return baselineFile
            }
        }
        null
    }

    private static List<File> sortedDirectories(File root) {
        File[] children = root.listFiles()
        List<File> directories = []
        if (children == null) {
            return directories
        }
        for (File child : children) {
            if (child.isDirectory()) {
                directories << child
            }
        }
        directories.sort { File left, File right -> left.name <=> right.name }
        directories
    }

    private static void writeCsv(File outputFile, List<GuideSummary> results) {
        outputFile.parentFile.mkdirs()
        StringBuilder sb = new StringBuilder('guide,version,status,issueCount,details\n')
        for (GuideSummary summary : results) {
            List<String> details = summary.differences.isEmpty()
                    ? [summary.differencesInfo]
                    : summary.differences + [summary.differencesInfo]
            sb << sanitize(summary.guide) << ','
            sb << sanitize(summary.version) << ','
            sb << sanitize(summary.status ?: 'VERIFIED') << ','
            sb << summary.differences.size() << ','
            sb << sanitize(abbreviate(details.findAll { String value -> value != null && !value.isEmpty() }.join(' | '))) << '\n'
        }
        outputFile.text = sb.toString()
    }

    private static String normalizeText(String value) {
        (value ?: '').replaceAll(/\s+/, ' ').trim()
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

    private static boolean isHardFailMode(Project project) {
        String mode = (project.findProperty('verificationMode') ?: '') as String
        mode.equalsIgnoreCase('hard-fail')
    }

    private static final class Fingerprint {
        String sourcePath
        int totalHeadings
        int sectionCount
        int totalAnchors
        int codeBlockCount
        int imageCount
        List<String> headingTree = []
        Set<String> anchorIds = [] as LinkedHashSet<String>
    }

    private static final class GuideSummary {
        String guide
        String version
        String status
        boolean selfBaseline
        String differencesInfo = ''
        List<String> differences = []
    }
}

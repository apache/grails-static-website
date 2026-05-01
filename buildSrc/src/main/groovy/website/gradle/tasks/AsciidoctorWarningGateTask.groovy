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
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.logging.StandardOutputListener

import grails.doc.gradle.PublishGuideTask

import java.util.regex.Pattern

/**
 * Scans captured PublishGuide output for unallowlisted warnings and errors and
 * writes a per-guide summary to {@code build/reports/asciidoctor-warning-gate.csv}.
 */
@CompileStatic
class AsciidoctorWarningGateTask extends DefaultTask {

    static final String NAME = 'asciidoctorWarningGate'
    static final String GROUP = 'migration'

    private static final Pattern WARNING_PATTERN = ~/(?im)^.*(\[WARNING\]|\[ERROR\]|asciidoctor:\s*(WARNING|ERROR|FAIL)|\bWARN(?:ING)?:|\bERROR:|java\.lang\.\w+(?:Error|Exception)|java\.io\.\w+(?:Error|Exception)|java\.util\.\w+(?:Error|Exception)|org\.\w+(?:\.\w+)*\.\w+(?:Error|Exception)|^.*Caused by:)/

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty logDir = project.objects.directoryProperty()

    @OutputFile
    final RegularFileProperty reportFile = project.objects.fileProperty()

    @Input
    final Property<Boolean> failOnViolation = project.objects.property(Boolean)

    @Input
    final ListProperty<String> guideTaskMappings = project.objects.listProperty(String)

    @Input
    @Optional
    final ListProperty<String> allowlistPatterns = project.objects.listProperty(String)

    @TaskAction
    void gate() {
        File logsRoot = logDir.get().asFile
        File report = reportFile.get().asFile

        Map<String, GuideTaskMetadata> metadataByTaskName = parseGuideTaskMappings(guideTaskMappings.get())
        List<Pattern> compiledAllowlist = compileAllowlist(allowlistPatterns.getOrElse([] as List<String>))
        List<GuideSummary> results = []

        for (GuideTaskMetadata metadata : metadataByTaskName.values()) {
            File logFile = new File(logsRoot, "asciidoctor-${metadata.taskName}.log")
            String status
            int issueCount
            String details
            if (!logFile.isFile()) {
                status = 'REVIEW'
                issueCount = 0
                details = "Missing captured log output for ${metadata.taskName}; rerun the renderer or invalidate Gradle caches before trusting this verdict.".toString()
            } else {
                List<String> violations = findViolations(logFile, compiledAllowlist)
                status = statusFor(violations.isEmpty())
                issueCount = violations.size()
                details = violations.isEmpty()
                        ? 'No unallowlisted warning or error lines detected.'
                        : abbreviate(violations.join(' | '))
            }
            results << new GuideSummary(
                    guide: metadata.guide,
                    version: metadata.version,
                    status: status,
                    issueCount: issueCount,
                    details: details,
            )
        }

        writeCsv(report, results)

        List<GuideSummary> failed = results.findAll { GuideSummary result -> result.status == 'FAILED' } as List<GuideSummary>
        if (!failed.isEmpty()) {
            throw new GradleException("AsciiDoctor warnings detected. See ${report.absolutePath}.")
        }
    }

    private static List<Pattern> compileAllowlist(List<String> patterns) {
        List<Pattern> compiled = []
        for (String raw : patterns) {
            String trimmed = raw?.trim()
            if (trimmed) {
                compiled.add(Pattern.compile(trimmed))
            }
        }
        compiled
    }

    static TaskProvider<AsciidoctorWarningGateTask> register(Project project) {
        wireLogCapture(project)
        project.tasks.register(NAME, AsciidoctorWarningGateTask) { AsciidoctorWarningGateTask task ->
            task.group = GROUP
            task.description = 'Captures PublishGuide output and summarizes unallowlisted AsciiDoctor warnings and errors.'
            task.notCompatibleWithConfigurationCache('Reads per-guide log capture emitted by vendored PublishGuide tasks and their finalizers.')
            task.logDir.convention(project.layout.buildDirectory.dir('logs'))
            task.reportFile.convention(project.layout.buildDirectory.file('reports/asciidoctor-warning-gate.csv'))
            task.failOnViolation.convention(isHardFailMode(project))
            task.guideTaskMappings.set(buildGuideTaskMappings(project))
            task.allowlistPatterns.convention(parseAllowlistProperty(project))
            task.dependsOn('buildAllGuides')
        }
    }

    private static void wireLogCapture(Project project) {
        // The listener is added in doFirst on each PublishGuide task. We deliberately
        // do NOT register a finalizer cleanup task: removing the listener would
        // require the cleanup task to access the renderTask's extensions/logging at
        // execution time, which Gradle's configuration cache disallows when the
        // cleanup is wired across tasks. Leaving the listener attached for the
        // remainder of the JVM lifetime is harmless: each `gradle` invocation runs
        // in a single-use Daemon (see publish.yml: --no-daemon) so the listener is
        // GC'd when the process exits, never crossing build boundaries.
        project.tasks.withType(PublishGuideTask).configureEach { PublishGuideTask renderTask ->
            File logsRoot = project.layout.buildDirectory.dir('logs').get().asFile
            File logFile = new File(logsRoot, "asciidoctor-${renderTask.name}.log")

            renderTask.doFirst {
                logsRoot.mkdirs()
                logFile.text = ''

                StandardOutputListener writeListener = ({ CharSequence message ->
                    logFile << message
                } as StandardOutputListener)

                renderTask.logging.addStandardOutputListener(writeListener)
                renderTask.logging.addStandardErrorListener(writeListener)
            }
        }
    }

    private static List<String> findViolations(File logFile, List<Pattern> excludeAllowlist) {
        List<String> violations = []
        int lineNumber = 0
        logFile.eachLine('UTF-8') { String line ->
            lineNumber++
            if (WARNING_PATTERN.matcher(line).find() && !isAllowlisted(line, excludeAllowlist)) {
                violations.add("${logFile.name}:${lineNumber}:${sanitize(line)}".toString())
            }
        }
        violations
    }

    private static List<String> parseAllowlistProperty(Project project) {
        Object raw = project.findProperty('asciidoctorAllowlist')
        if (!raw) {
            return [] as List<String>
        }
        ((raw as String).split(',') as List<String>).collect { String token -> token?.trim() }.findAll { String token -> token } as List<String>
    }

    private static boolean isAllowlisted(String line, List<Pattern> excludeAllowlist) {
        for (Pattern pattern : excludeAllowlist) {
            if (pattern.matcher(line).find()) {
                return true
            }
        }
        false
    }

    private static List<String> buildGuideTaskMappings(Project project) {
        List<String> mappings = []
        File buildDir = project.layout.buildDirectory.get().asFile
        String buildPrefix = buildDir.absolutePath.replace('\\', '/')
        project.tasks.withType(PublishGuideTask).each { PublishGuideTask renderTask ->
            File targetDir = renderTask.targetDir.get().asFile
            String normalizedTarget = targetDir.absolutePath.replace('\\', '/')
            String relativePath = normalizedTarget.startsWith(buildPrefix)
                    ? normalizedTarget.substring(buildPrefix.length()).replaceFirst('^/+', '')
                    : normalizedTarget
            List<String> segments = relativePath.tokenize('/')
            if (segments.size() >= 4 && segments[0] == 'dist' && segments[1] == 'guides') {
                mappings.add("${renderTask.name}|${segments[2]}|${segments[3]}".toString())
            }
        }
        mappings
    }

    private static Map<String, GuideTaskMetadata> parseGuideTaskMappings(List<String> mappings) {
        Map<String, GuideTaskMetadata> metadataByTaskName = [:]
        for (String mapping : mappings) {
            List<String> parts = mapping.tokenize('|')
            if (parts.size() == 3) {
                metadataByTaskName[parts[0]] = new GuideTaskMetadata(
                        taskName: parts[0],
                        guide: parts[1],
                        version: parts[2],
                )
            }
        }
        metadataByTaskName
    }

    private String statusFor(boolean clean) {
        if (clean) {
            return 'VERIFIED'
        }
        failOnViolation.get() ? 'FAILED' : 'REVIEW'
    }

    private static boolean isHardFailMode(Project project) {
        String mode = (project.findProperty('verificationMode') ?: '') as String
        mode.equalsIgnoreCase('hard-fail')
    }

    private static void writeCsv(File outputFile, List<GuideSummary> results) {
        outputFile.parentFile.mkdirs()
        StringBuilder sb = new StringBuilder('guide,version,status,issueCount,details\n')
        for (GuideSummary result : results) {
            sb << sanitize(result.guide) << ','
            sb << sanitize(result.version) << ','
            sb << sanitize(result.status) << ','
            sb << result.issueCount << ','
            sb << sanitize(result.details) << '\n'
        }
        outputFile.text = sb.toString()
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

    private static final class GuideTaskMetadata {
        String taskName
        String guide
        String version
    }

    private static final class GuideSummary {
        String guide
        String version
        String status
        int issueCount
        String details
    }
}

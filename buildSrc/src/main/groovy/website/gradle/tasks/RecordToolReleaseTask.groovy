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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * Maintains entries in the {@code tools:} block of {@code conf/releases.yml}.
 * Tools (e.g. the IntelliJ plugin) are Apache-released artifacts versioned
 * independently of any Grails major, rendered in the "Developer Tools" card on
 * the downloads page.
 *
 * <p>Invoked from {@code .github/workflows/release-tool.yml} on a
 * {@code workflow_dispatch}. The task supports two operations on a single
 * {@code artifactId}, automatically picking the right one based on what
 * already exists in the file:
 *
 * <ol>
 *   <li><strong>Bump existing entry</strong> - the tool is already listed. The
 *   {@code version:} field is rewritten in place and the optional descriptor
 *   flags are ignored.
 *   <pre>
 *     ./gradlew recordToolRelease \
 *         -PartifactId=grails-intellij-plugin \
 *         -PartifactVersion=262.1.2
 *   </pre></li>
 *   <li><strong>Add new tool</strong> - the artifactId is not yet listed. A new
 *   entry is appended to the end of the {@code tools:} section. All three
 *   descriptor flags ({@code -PmirrorDirectory}, {@code -PreleaseNotesRepo},
 *   {@code -PdisplayName}) are required.</li>
 * </ol>
 *
 * <p>Like {@link RecordCompanionReleaseTask}, edits are line-based so existing
 * comments and indentation in {@code releases.yml} are preserved, and the
 * descriptor flags are ignored on the bump path so already-published URLs
 * only change through a manual PR.
 */
@CompileStatic
abstract class RecordToolReleaseTask extends DefaultTask {

    static final String NAME = 'recordToolRelease'
    static final String GROUP = 'release'

    @Input
    abstract Property<String> getArtifactId()

    @Input
    abstract Property<String> getArtifactVersion()

    @Input
    @Optional
    abstract Property<String> getMirrorDirectory()

    @Input
    @Optional
    abstract Property<String> getReleaseNotesRepo()

    @Input
    @Optional
    abstract Property<String> getDisplayName()

    @OutputFile
    abstract RegularFileProperty getReleasesYaml()

    @TaskAction
    void recordToolRelease() {
        String artifact = artifactId.get()?.trim()
        String version = artifactVersion.get()?.trim()
        if (!artifact || !version) {
            throw new GradleException(
                    'recordToolRelease requires -PartifactId=<name> -PartifactVersion=<version>.')
        }

        File yaml = releasesYaml.get().asFile
        if (!yaml.isFile()) {
            throw new GradleException(
                    "Expected ${yaml.absolutePath} to exist. Did the working tree drift?".toString())
        }

        List<String> lines = yaml.readLines('UTF-8')
        Layout layout = scanLayout(lines, artifact)

        if (layout.toolsHeaderIdx < 0) {
            throw new GradleException(
                    "${yaml.name} has no tools: section. Add one above coreReleases: before recording a tool release.".toString())
        }

        String mode
        if (layout.targetVersionIdx >= 0) {
            String original = lines[layout.targetVersionIdx]
            lines[layout.targetVersionIdx] =
                    "${leadingWhitespace(original)}version: '${version}'".toString()
            mode = 'bump'
        } else {
            requireDescriptorFlags(artifact)
            lines.addAll(layout.toolsSectionEndIdx, renderEntry(artifact, version))
            mode = 'new-tool'
        }

        yaml.text = lines.join(System.lineSeparator()) + System.lineSeparator()
        logger.lifecycle("Updated tool ${artifact} to ${version} in ${yaml.name} (${mode}).".toString())
    }

    private void requireDescriptorFlags(String artifact) {
        List<String> missing = []
        if (!mirrorDirectory.getOrElse('').trim()) missing << '-PmirrorDirectory=<sub-path>'
        if (!releaseNotesRepo.getOrElse('').trim()) missing << '-PreleaseNotesRepo=<org/repo>'
        if (!displayName.getOrElse('').trim()) missing << '-PdisplayName=<Human Readable Name>'
        if (missing) {
            throw new GradleException(
                    "First-time entry: artifactId=${artifact} is not yet listed under tools:. ".toString() +
                            "Required descriptor flags missing: ${missing.join(', ')}. ".toString() +
                            'Pass all three so the new entry can be rendered (mirror URL, release-notes link, display name).')
        }
    }

    private List<String> renderEntry(String artifact, String version) {
        return [
                "  - artifactId: ${artifact}".toString(),
                "    version: '${version}'".toString(),
                "    mirrorDirectory: ${mirrorDirectory.get().trim()}".toString(),
                "    releaseNotesRepo: ${releaseNotesRepo.get().trim()}".toString(),
                "    displayName: ${displayName.get().trim()}".toString(),
        ]
    }

    private static String leadingWhitespace(String line) {
        StringBuilder ws = new StringBuilder()
        for (int j = 0; j < line.length(); j++) {
            char ch = line.charAt(j)
            if (ch == ' ' as char || ch == '\t' as char) {
                ws.append(ch)
            } else {
                break
            }
        }
        return ws.toString()
    }

    private static Layout scanLayout(List<String> lines, String targetArtifact) {
        Layout layout = new Layout()
        String artifactMarker = "artifactId: ${targetArtifact}".toString()
        boolean inTools = false
        boolean inTargetArtifact = false

        for (int i = 0; i < lines.size(); i++) {
            String line = lines[i]
            String trimmed = line.trim()
            String normalized = trimmed.startsWith('- ') ? trimmed.substring(2) : trimmed

            if (!inTools) {
                if (trimmed.startsWith('tools:') && !line.startsWith(' ') && !line.startsWith('\t')) {
                    inTools = true
                    layout.toolsHeaderIdx = i
                }
                continue
            }

            boolean isTopLevelKey = trimmed && !line.startsWith(' ') && !line.startsWith('\t') && !trimmed.startsWith('#')
            if (isTopLevelKey) {
                layout.toolsSectionEndIdx = trimToLastMeaningfulLine(lines, i)
                return layout
            }

            if (normalized == artifactMarker) {
                inTargetArtifact = true
                continue
            }
            if (normalized.startsWith('artifactId: ')) {
                inTargetArtifact = false
                continue
            }
            if (inTargetArtifact && trimmed.startsWith('version:') && layout.targetVersionIdx < 0) {
                layout.targetVersionIdx = i
            }
        }

        if (layout.toolsHeaderIdx >= 0) {
            layout.toolsSectionEndIdx = trimToLastMeaningfulLine(lines, lines.size())
        }
        return layout
    }

    private static int trimToLastMeaningfulLine(List<String> lines, int upperExclusive) {
        int idx = upperExclusive
        while (idx > 0 && lines[idx - 1].trim().isEmpty()) {
            idx--
        }
        return idx
    }

    private static class Layout {
        int toolsHeaderIdx = -1
        int toolsSectionEndIdx = -1
        int targetVersionIdx = -1
    }

    static TaskProvider<RecordToolReleaseTask> register(Project project) {
        project.tasks.register(NAME, RecordToolReleaseTask) { task ->
            task.group = GROUP
            task.description =
                    'Maintain a tool entry (e.g. the IntelliJ plugin) in conf/releases.yml. ' +
                            'Required: -PartifactId=name -PartifactVersion=version. ' +
                            'For first-time entries also pass: -PmirrorDirectory=path -PreleaseNotesRepo=org/repo -PdisplayName=Label.'
            task.artifactId.set(
                    project.providers.gradleProperty('artifactId').orElse(''))
            task.artifactVersion.set(
                    project.providers.gradleProperty('artifactVersion').orElse(''))
            task.mirrorDirectory.set(
                    project.providers.gradleProperty('mirrorDirectory').orElse(''))
            task.releaseNotesRepo.set(
                    project.providers.gradleProperty('releaseNotesRepo').orElse(''))
            task.displayName.set(
                    project.providers.gradleProperty('displayName').orElse(''))
            task.releasesYaml.set(
                    project.rootProject.layout.projectDirectory.file('conf/releases.yml'))
        }
    }
}

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
 * Maintains entries in the {@code companionArtifacts:} block of
 * {@code conf/releases.yml}. Companion artifacts (Spring Security, Redis,
 * Quartz, GitHub Actions, Gradle Publish, ...) ship independently of Grails
 * core, so each one has its own release cadence.
 *
 * <p>Invoked from {@code .github/workflows/release-companion.yml} on a
 * {@code workflow_dispatch}. The task supports three operations on a single
 * {@code (grailsMajor, artifactId)} pair, automatically picking the right one
 * based on what already exists in the file:
 *
 * <ol>
 *   <li><strong>Bump existing entry</strong> - the artifact is already listed
 *   under its major. The {@code version:} field is rewritten in place and
 *   the optional descriptor flags are ignored.
 *   <pre>
 *     ./gradlew recordCompanionRelease \
 *         -PgrailsMajor=7 \
 *         -PartifactId=grails-redis \
 *         -PartifactVersion=5.0.2
 *   </pre></li>
 *   <li><strong>Add new artifact to an existing major</strong> - the
 *   {@code 'N':} block exists but does not yet contain this artifactId. A new
 *   entry is appended to the end of that block. All three descriptor flags
 *   ({@code -PmirrorDirectory}, {@code -PreleaseNotesRepo},
 *   {@code -PdisplayName}) are required.</li>
 *   <li><strong>Bootstrap a brand-new major</strong> - no {@code 'N':} block
 *   exists yet. A new major block is appended to the end of the
 *   {@code companionArtifacts:} section (preserving the trailing blank line
 *   that separates it from {@code coreReleases:}) containing exactly one
 *   entry. All three descriptor flags are required.</li>
 * </ol>
 *
 * <p>The task performs line-based edits so existing comments and indentation
 * in {@code releases.yml} are preserved (a YAML round-trip via SnakeYAML
 * would erase them). The descriptor flags are accepted but ignored on the
 * bump path because changing them mid-major would break URLs that downstream
 * pages and crawlers may have already indexed - those changes intentionally
 * require a manual PR.
 */
@CompileStatic
abstract class RecordCompanionReleaseTask extends DefaultTask {

    static final String NAME = 'recordCompanionRelease'
    static final String GROUP = 'release'

    @Input
    abstract Property<String> getGrailsMajor()

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
    void recordCompanionRelease() {
        String major = grailsMajor.get()?.trim()
        String artifact = artifactId.get()?.trim()
        String version = artifactVersion.get()?.trim()
        if (!major || !artifact || !version) {
            throw new GradleException(
                    'recordCompanionRelease requires -PgrailsMajor=<N> -PartifactId=<name> -PartifactVersion=<X.Y.Z>.')
        }

        File yaml = releasesYaml.get().asFile
        if (!yaml.isFile()) {
            throw new GradleException(
                    "Expected ${yaml.absolutePath} to exist. Did the working tree drift?".toString())
        }

        List<String> lines = yaml.readLines('UTF-8')
        Layout layout = scanLayout(lines, major, artifact)

        if (layout.companionHeaderIdx < 0) {
            throw new GradleException(
                    "${yaml.name} has no companionArtifacts: section. The schema migration commit added it; ".toString() +
                            'did the working tree drift?')
        }

        String mode
        if (layout.targetArtifactVersionIdx >= 0) {
            String original = lines[layout.targetArtifactVersionIdx]
            lines[layout.targetArtifactVersionIdx] =
                    "${leadingWhitespace(original)}version: '${version}'".toString()
            mode = 'bump'
        } else if (layout.targetMajorEndIdx >= 0) {
            requireDescriptorFlags(major, artifact, false)
            lines.addAll(layout.targetMajorEndIdx, renderEntry(artifact, version))
            mode = 'new-artifact'
        } else {
            requireDescriptorFlags(major, artifact, true)
            lines.addAll(layout.companionSectionEndIdx, renderMajorBlock(major, artifact, version))
            mode = 'new-major'
        }

        yaml.text = lines.join(System.lineSeparator()) + System.lineSeparator()
        logger.lifecycle(
                "Updated companion ${artifact} to ${version} for Grails ${major} in ${yaml.name} (${mode}).".toString())
    }

    private void requireDescriptorFlags(String major, String artifact, boolean newMajor) {
        String mirror = mirrorDirectory.getOrElse('').trim()
        String notes = releaseNotesRepo.getOrElse('').trim()
        String display = displayName.getOrElse('').trim()
        List<String> missing = []
        if (!mirror) missing << '-PmirrorDirectory=<sub-path>'
        if (!notes) missing << '-PreleaseNotesRepo=<org/repo>'
        if (!display) missing << '-PdisplayName=<Human Readable Name>'
        if (missing) {
            String context = newMajor
                    ? "no '${major}': block exists yet under companionArtifacts:".toString()
                    : "artifactId=${artifact} is not yet listed under '${major}':".toString()
            throw new GradleException(
                    "First-time entry: ${context}. Required descriptor flags missing: ${missing.join(', ')}. ".toString() +
                            'Pass all three so the new entry can be rendered (mirror URL, release-notes link, display name).')
        }
    }

    private List<String> renderEntry(String artifact, String version) {
        return [
                "    - artifactId: ${artifact}".toString(),
                "      version: '${version}'".toString(),
                "      mirrorDirectory: ${mirrorDirectory.get().trim()}".toString(),
                "      releaseNotesRepo: ${releaseNotesRepo.get().trim()}".toString(),
                "      displayName: ${displayName.get().trim()}".toString(),
        ]
    }

    private List<String> renderMajorBlock(String major, String artifact, String version) {
        List<String> block = ["  '${major}':".toString()] as List<String>
        block.addAll(renderEntry(artifact, version))
        return block
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

    private static Layout scanLayout(List<String> lines, String targetMajor, String targetArtifact) {
        Layout layout = new Layout()
        String majorMarker = "'${targetMajor}':".toString()
        String artifactMarker = "artifactId: ${targetArtifact}".toString()
        boolean inCompanion = false
        boolean inTargetMajor = false
        boolean inTargetArtifact = false

        for (int i = 0; i < lines.size(); i++) {
            String line = lines[i]
            String trimmed = line.trim()
            String normalized = trimmed.startsWith('- ') ? trimmed.substring(2) : trimmed

            if (!inCompanion) {
                if (trimmed == 'companionArtifacts:' || trimmed.startsWith('companionArtifacts:')) {
                    inCompanion = true
                    layout.companionHeaderIdx = i
                }
                continue
            }

            boolean isTopLevelKey = trimmed && !line.startsWith(' ') && !line.startsWith('\t') && !trimmed.startsWith('#')
            if (isTopLevelKey) {
                int sectionEnd = trimToLastMeaningfulLine(lines, i)
                if (inTargetMajor && layout.targetMajorEndIdx < 0) {
                    layout.targetMajorEndIdx = sectionEnd
                }
                layout.companionSectionEndIdx = sectionEnd
                return layout
            }

            if (trimmed ==~ /^'\d+':$/) {
                if (inTargetMajor && layout.targetMajorEndIdx < 0) {
                    layout.targetMajorEndIdx = trimToLastMeaningfulLine(lines, i)
                }
                inTargetMajor = (trimmed == majorMarker)
                inTargetArtifact = false
                continue
            }

            if (inTargetMajor && normalized == artifactMarker) {
                inTargetArtifact = true
                continue
            }
            if (inTargetMajor && normalized.startsWith('artifactId: ')) {
                inTargetArtifact = false
                continue
            }
            if (inTargetArtifact && trimmed.startsWith('version:') && layout.targetArtifactVersionIdx < 0) {
                layout.targetArtifactVersionIdx = i
            }
        }

        if (layout.companionHeaderIdx >= 0 && layout.companionSectionEndIdx < 0) {
            layout.companionSectionEndIdx = trimToLastMeaningfulLine(lines, lines.size())
        }
        if (inTargetMajor && layout.targetMajorEndIdx < 0) {
            layout.targetMajorEndIdx = layout.companionSectionEndIdx
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
        int companionHeaderIdx = -1
        int companionSectionEndIdx = -1
        int targetMajorEndIdx = -1
        int targetArtifactVersionIdx = -1
    }

    static TaskProvider<RecordCompanionReleaseTask> register(Project project) {
        project.tasks.register(NAME, RecordCompanionReleaseTask) { task ->
            task.group = GROUP
            task.description =
                    'Maintain a companion artifact entry in conf/releases.yml. ' +
                            'Required: -PgrailsMajor=N -PartifactId=name -PartifactVersion=X.Y.Z. ' +
                            'For first-time entries also pass: -PmirrorDirectory=path -PreleaseNotesRepo=org/repo -PdisplayName=Label.'
            task.grailsMajor.set(
                    project.providers.gradleProperty('grailsMajor').orElse(''))
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

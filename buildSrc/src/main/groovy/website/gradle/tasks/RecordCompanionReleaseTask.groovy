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
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * Bumps the {@code version:} field of an existing companion artifact entry in
 * {@code conf/releases.yml}. Companion artifacts (Spring Security, Redis,
 * Quartz, GitHub Actions, Gradle Publish, ...) ship independently of Grails
 * core, so each one has its own release cadence and needs its own bump task.
 *
 * <p>Invoked from {@code .github/workflows/release-companion.yml} on a
 * {@code workflow_dispatch}, with three inputs:
 * <pre>
 *   ./gradlew recordCompanionRelease \
 *       -PgrailsMajor=7 \
 *       -PartifactId=grails-redis \
 *       -PartifactVersion=5.0.2
 * </pre>
 *
 * <p>The task performs a line-based update so existing comments and
 * indentation in {@code releases.yml} are preserved (a YAML round-trip via
 * SnakeYAML would erase them). It looks for the {@code 'N':} block under
 * {@code companionArtifacts:}, then within that block locates the entry whose
 * {@code artifactId:} matches and rewrites the next {@code version:} line.
 *
 * <p>This task only updates an existing entry. First-time addition of a brand
 * new companion artifact for a major requires a manual edit of
 * {@code conf/releases.yml} (e.g. a one-time PR when a new plugin is
 * provisioned by the PMC), since the schema of a new entry can vary
 * (mirrorDirectory, releaseNotesRepo, displayName all need PMC-supplied
 * values).
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
        boolean inCompanionSection = false
        boolean inMajorSection = false
        boolean foundArtifact = false
        boolean updated = false
        String majorMarker = "'${major}':".toString()
        String artifactMarker = "artifactId: ${artifact}".toString()

        for (int i = 0; i < lines.size(); i++) {
            String line = lines[i]
            String trimmed = line.trim()
            // Strip the YAML list-item prefix "- " so we can compare against
            // an artifactId without worrying about which entry happens to be
            // the first (sequence-bullet-bearing) one in the major's block.
            String normalized = trimmed.startsWith('- ') ? trimmed.substring(2) : trimmed

            if (trimmed == 'companionArtifacts:' || trimmed.startsWith('companionArtifacts:')) {
                inCompanionSection = true
                continue
            }

            // Leaving the companionArtifacts: section once we hit another
            // top-level key (a non-blank line with no leading whitespace).
            if (inCompanionSection && trimmed && !line.startsWith(' ') && !line.startsWith('\t')
                    && !trimmed.startsWith('#')) {
                inCompanionSection = false
                inMajorSection = false
            }

            if (inCompanionSection && trimmed.startsWith(majorMarker)) {
                inMajorSection = true
                continue
            }

            // Within companionArtifacts, hitting another quoted single-segment
            // major key (e.g. "'8':") means we've left our major.
            if (inMajorSection && trimmed ==~ /^'\d+':$/ && trimmed != majorMarker) {
                inMajorSection = false
            }

            if (inMajorSection && !foundArtifact && normalized == artifactMarker) {
                foundArtifact = true
                continue
            }

            if (foundArtifact && trimmed.startsWith('version:')) {
                // Preserve the original indentation; rewrite only the value
                // and (re-apply) single-quote wrapping for consistency with
                // the rest of the companion entries written by the previous
                // commit's data migration.
                String leadingWs = ''
                for (int j = 0; j < line.length(); j++) {
                    char ch = line.charAt(j)
                    if (ch == ' ' as char || ch == '\t' as char) {
                        leadingWs += ch
                    } else {
                        break
                    }
                }
                lines[i] = "${leadingWs}version: '${version}'".toString()
                updated = true
                break
            }
        }

        if (!updated) {
            throw new GradleException(
                    "Could not find companionArtifact entry artifactId=${artifact} under Grails major '${major}' in ${yaml.name}. ".toString() +
                            'For a brand-new companion artifact, add the entry to conf/releases.yml manually first.')
        }

        yaml.text = lines.join(System.lineSeparator()) + System.lineSeparator()
        logger.lifecycle("Updated companion ${artifact} to ${version} for Grails ${major} in ${yaml.name}.")
    }

    static TaskProvider<RecordCompanionReleaseTask> register(Project project) {
        project.tasks.register(NAME, RecordCompanionReleaseTask) { task ->
            task.group = GROUP
            task.description =
                    'Bump a companion artifact version in conf/releases.yml. ' +
                            'Pass via -PgrailsMajor=N -PartifactId=name -PartifactVersion=X.Y.Z.'
            task.grailsMajor.set(
                    project.providers.gradleProperty('grailsMajor').orElse(''))
            task.artifactId.set(
                    project.providers.gradleProperty('artifactId').orElse(''))
            task.artifactVersion.set(
                    project.providers.gradleProperty('artifactVersion').orElse(''))
            task.releasesYaml.set(
                    project.rootProject.layout.projectDirectory.file('conf/releases.yml'))
        }
    }
}

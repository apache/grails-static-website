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

import java.text.SimpleDateFormat

/**
 * Appends a new Grails release entry to {@code conf/releases.yml}. Replaces
 * the legacy {@code release.sh} bash script per the Gradle-only operational
 * scripts policy (HANDOFF.md owner directive 2026-04-30).
 *
 * <p>Invoked from the {@code .github/workflows/release.yml} workflow on the
 * release-day workflow_dispatch event. The release version is supplied via
 * the {@code -PreleaseVersion=X.Y.Z} Gradle property, which the workflow
 * forwards from its {@code grails_version} input.
 *
 * <p>Output format mirrors the existing entries in {@code conf/releases.yml}:
 * an unquoted {@code version} value followed by an English-locale
 * {@code publicationDate} of the form {@code MMM dd, yyyy} (e.g.
 * {@code May 02, 2026}), with a trailing blank line for visual separation.
 */
@CompileStatic
abstract class RecordReleaseTask extends DefaultTask {

    static final String NAME = 'recordRelease'
    static final String GROUP = 'release'

    @Input
    abstract Property<String> getReleaseVersion()

    @OutputFile
    abstract RegularFileProperty getReleasesYaml()

    @TaskAction
    void recordRelease() {
        String version = releaseVersion.get()?.trim()
        if (!version) {
            throw new GradleException(
                    "releaseVersion is required. Pass via -PreleaseVersion=<X.Y.Z>.")
        }

        SimpleDateFormat fmt = new SimpleDateFormat('MMM dd, yyyy', Locale.ENGLISH)
        String today = fmt.format(new Date())

        File yaml = releasesYaml.get().asFile
        if (!yaml.isFile()) {
            throw new GradleException(
                    "Expected ${yaml.absolutePath} to exist. Did the working tree drift?")
        }

        StringBuilder appended = new StringBuilder()
        appended.append('  - version: ').append(version).append('\n')
        appended.append('    publicationDate: ').append(today).append('\n')
        appended.append('\n')

        yaml.append(appended.toString(), 'UTF-8')

        logger.lifecycle("Recorded release ${version} at ${today} in ${yaml.name}.")
    }

    static TaskProvider<RecordReleaseTask> register(Project project) {
        project.tasks.register(NAME, RecordReleaseTask) { task ->
            task.group = GROUP
            task.description =
                    'Append a new Grails release entry to conf/releases.yml. Pass version via -PreleaseVersion=X.Y.Z.'
            task.releaseVersion.set(
                    project.providers.gradleProperty('releaseVersion').orElse(''))
            task.releasesYaml.set(
                    project.rootProject.layout.projectDirectory.file('conf/releases.yml'))
        }
    }
}

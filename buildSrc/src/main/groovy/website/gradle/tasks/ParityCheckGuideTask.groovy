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
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import website.qa.AdHocFixtureDiff

/**
 * Compares a locally rendered guide HTML against a baseline snapshot taken
 * from the legacy {@code https://guides.grails.org/...} site, emitting a
 * structural-diff report to {@code build/reports/parity/<name>/<version>.md}
 * and (optionally) failing the build when divergence is severe.
 *
 * <p>Wired by {@link website.gradle.RenderGuidesPlugin} as
 * {@code parityCheckGuide_<name>_<version>}; the aggregate is
 * {@code parityCheckAllGuides}. Defaults assume the renderer's
 * single-page output ({@code single.html}) is the parity target since the
 * legacy site renders all chapters into one page.</p>
 *
 * <p>Exit policy: by default this task fails the build if the diff
 * report contains hard-fail conditions (see
 * {@link AdHocFixtureDiff.Report#isStructurallyEquivalent()}). Set
 * {@code -PparityFailOnDiff=false} to demote failures to warnings while
 * the renderer config is being tuned.</p>
 */
@CompileStatic
class ParityCheckGuideTask extends DefaultTask {

    static final String NAME = 'parityCheckGuide'
    static final String GROUP = 'documentation'

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty localFile = project.objects.fileProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty baselineFile = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty reportFile = project.objects.fileProperty()

    @Input
    final Property<Boolean> failOnDiff = project.objects.property(Boolean).convention(true)

    @Input
    final Property<String> guideLabel = project.objects.property(String).convention('guide')

    @TaskAction
    void check() {
        File local = localFile.get().asFile
        File baseline = baselineFile.get().asFile
        File report = reportFile.get().asFile

        if (!local.isFile()) {
            throw new GradleException("Local file does not exist: ${local} -- run the corresponding renderGuide task first.")
        }
        if (!baseline.isFile()) {
            throw new GradleException("Baseline file does not exist: ${baseline} -- vendor a snapshot from https://guides.grails.org/ into buildSrc/src/test/resources/parity-baseline/.")
        }

        report.parentFile.mkdirs()
        AdHocFixtureDiff.Report result = AdHocFixtureDiff.compare(local, baseline)
        report.text = result.toHumanReport()

        logger.lifecycle("Renderer parity report for ${guideLabel.get()}: ${report.absolutePath}")
        if (!result.differences.isEmpty()) {
            logger.warn("${result.differences.size()} parity differences found:")
            result.differences.each { logger.warn("  - ${it}") }
        } else {
            logger.lifecycle('Renderer parity OK -- structurally equivalent within thresholds.')
        }

        if (!result.isStructurallyEquivalent() && failOnDiff.get()) {
            throw new GradleException("Renderer parity check FAILED for ${guideLabel.get()}. " +
                    "See ${report.absolutePath}. " +
                    'Re-run with -PparityFailOnDiff=false to demote to warnings while iterating.')
        }
    }
}

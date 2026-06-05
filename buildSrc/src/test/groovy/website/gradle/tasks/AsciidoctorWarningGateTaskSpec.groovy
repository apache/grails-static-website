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

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder

import grails.doc.gradle.PublishGuideTask
import spock.lang.Specification
import spock.lang.TempDir

class AsciidoctorWarningGateTaskSpec extends Specification {

    @TempDir
    File tempDir

    def 'captures a clean log as VERIFIED'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        String renderTaskName = registerRenderTask(project, 'demo-guide', '1')
        AsciidoctorWarningGateTask.register(project)
        File logsDir = new File(project.buildDir, 'logs')
        logsDir.mkdirs()
        new File(logsDir, "asciidoctor-${renderTaskName}.log").text = '[INFO] build completed\n'

        when:
        def gateTask = project.tasks.getByName(AsciidoctorWarningGateTask.NAME) as AsciidoctorWarningGateTask
        gateTask.gate()

        then:
        gateTask.group == AsciidoctorWarningGateTask.GROUP
        gateTask.reportFile.get().asFile.text.contains('demo-guide,1,VERIFIED,0')
    }

    def 'writes only the header when no logs directory exists'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        AsciidoctorWarningGateTask.register(project)

        when:
        def gateTask = project.tasks.getByName(AsciidoctorWarningGateTask.NAME) as AsciidoctorWarningGateTask
        gateTask.gate()

        then:
        gateTask.reportFile.get().asFile.readLines() == ['guide,version,status,issueCount,details']
    }

    def 'throws in hard-fail mode when warning lines are detected'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        String renderTaskName = registerRenderTask(project, 'demo-guide', '1')
        AsciidoctorWarningGateTask.register(project)
        File logsDir = new File(project.buildDir, 'logs')
        logsDir.mkdirs()
        new File(logsDir, "asciidoctor-${renderTaskName}.log").text = 'asciidoctor: WARNING: missing include target\n'
        def gateTask = project.tasks.getByName(AsciidoctorWarningGateTask.NAME) as AsciidoctorWarningGateTask
        gateTask.failOnViolation.set(true)

        when:
        gateTask.gate()

        then:
        def error = thrown(GradleException)
        error.message.contains('AsciiDoctor warnings detected')
        gateTask.reportFile.get().asFile.text.contains('demo-guide,1,FAILED,1')
    }

    private String registerRenderTask(def project, String guideName, String version) {
        project.tasks.register("renderGuide_${guideName}_${version}", PublishGuideTask) { PublishGuideTask task ->
            task.targetDir.set(project.layout.buildDirectory.dir("dist/guides/${guideName}/${version}"))
            task.sourceDir.set(project.layout.projectDirectory)
            task.resourcesDir.set(project.layout.projectDirectory)
        }
        "renderGuide_${guideName}_${version}"
    }
}

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

import spock.lang.Specification
import spock.lang.TempDir

class AcceptanceReportTaskSpec extends Specification {

    @TempDir
    File tempDir

    def 'combines all gate outputs into a VERIFIED acceptance row'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        createGuideVersion(project)
        writeCsv(new File(project.buildDir, 'reports/asciidoctor-warning-gate.csv'), 'demo-guide,1,VERIFIED,0,clean')
        writeCsv(new File(project.buildDir, 'reports/crawl-built-guides.csv'), 'demo-guide,1,VERIFIED,0,clean')
        new File(project.buildDir, 'reports/csp-scan.md').with {
            parentFile.mkdirs()
            text = '# CSP Scan Report\n\n## Result: CLEAN\n'
        }
        AcceptanceReportTask.register(project)

        when:
        def task = project.tasks.getByName(AcceptanceReportTask.NAME) as AcceptanceReportTask
        task.report()

        then:
        task.reportFile.get().asFile.text.contains('demo-guide,1,VERIFIED,VERIFIED,VERIFIED,VERIFIED')
    }

    def 'writes only the header when there are no guide versions or reports'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        AcceptanceReportTask.register(project)

        when:
        def task = project.tasks.getByName(AcceptanceReportTask.NAME) as AcceptanceReportTask
        task.report()

        then:
        task.reportFile.get().asFile.readLines() == ['guide,version,asciidoctorWarningGate,crawlBuiltGuides,cspScan,verdict,details']
    }

    def 'throws in hard-fail mode when any gate reports FAILED'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        createGuideVersion(project)
        writeCsv(new File(project.buildDir, 'reports/asciidoctor-warning-gate.csv'), 'demo-guide,1,FAILED,1,warning found')
        writeCsv(new File(project.buildDir, 'reports/crawl-built-guides.csv'), 'demo-guide,1,VERIFIED,0,clean')
        new File(project.buildDir, 'reports/csp-scan.md').with {
            parentFile.mkdirs()
            text = '# CSP Scan Report\n\n## Result: CLEAN\n'
        }
        AcceptanceReportTask.register(project)
        def task = project.tasks.getByName(AcceptanceReportTask.NAME) as AcceptanceReportTask
        task.failOnViolation.set(true)

        when:
        task.report()

        then:
        def error = thrown(GradleException)
        error.message.contains('Acceptance report contains FAILED rows')
        task.reportFile.get().asFile.text.contains('demo-guide,1,FAILED,VERIFIED,VERIFIED,FAILED')
    }

    def 'maps guide-specific CSP report entries with windows separators to REVIEW'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        createGuideVersion(project)
        writeCsv(new File(project.buildDir, 'reports/asciidoctor-warning-gate.csv'), 'demo-guide,1,VERIFIED,0,clean')
        writeCsv(new File(project.buildDir, 'reports/crawl-built-guides.csv'), 'demo-guide,1,VERIFIED,0,clean')
        new File(project.buildDir, 'reports/csp-scan.md').with {
            parentFile.mkdirs()
            text = '# CSP Scan Report\n\nNon-allowlisted hosts found: 1\n\n## Violations\n\n- `guides\\demo-guide\\1\\guide\\single.html`\n'
        }
        AcceptanceReportTask.register(project)

        when:
        def task = project.tasks.getByName(AcceptanceReportTask.NAME) as AcceptanceReportTask
        task.report()

        then:
        task.reportFile.get().asFile.text.contains('demo-guide,1,VERIFIED,VERIFIED,REVIEW,REVIEW')
    }

    private void createGuideVersion(def project) {
        File versionDir = new File(project.buildDir, 'dist/guides/demo-guide/1')
        versionDir.mkdirs()
        new File(versionDir, 'index.html').text = '<html></html>'
    }

    private void writeCsv(File file, String row) {
        file.parentFile.mkdirs()
        file.text = 'guide,version,status,issueCount,details\n' + row + '\n'
    }
}

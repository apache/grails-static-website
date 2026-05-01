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

class StructuralDiffGuidesTaskSpec extends Specification {

    @TempDir
    File tempDir

    def 'extracts a structural fingerprint and marks the guide REVIEW until a production baseline exists'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        createStructuralFile(project, '''
<html>
  <body>
    <h1 id="intro">Intro</h1>
    <div class="sect1">
      <h2 id="details">Details</h2>
      <pre>code</pre>
      <img src="img.png" />
    </div>
  </body>
</html>
''')
        StructuralDiffGuidesTask.register(project)

        when:
        def task = project.tasks.getByName(StructuralDiffGuidesTask.NAME) as StructuralDiffGuidesTask
        task.diff()

        then:
        task.reportFile.get().asFile.text.contains('demo-guide,1,REVIEW,0')
        new File(project.buildDir, 'reports/structural-fingerprints/demo-guide/1.json').text.contains('Intro')
    }

    def 'writes only the header when no rendered guides exist'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        StructuralDiffGuidesTask.register(project)

        when:
        def task = project.tasks.getByName(StructuralDiffGuidesTask.NAME) as StructuralDiffGuidesTask
        task.diff()

        then:
        task.reportFile.get().asFile.readLines() == ['guide,version,status,issueCount,details']
    }

    def 'throws in hard-fail mode when the rendered file has no headings'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        createStructuralFile(project, '<html><body><div id="only-anchor"></div></body></html>')
        StructuralDiffGuidesTask.register(project)
        def task = project.tasks.getByName(StructuralDiffGuidesTask.NAME) as StructuralDiffGuidesTask
        task.failOnViolation.set(true)

        when:
        task.diff()

        then:
        def error = thrown(GradleException)
        error.message.contains('Structural guide differences detected')
        task.reportFile.get().asFile.text.contains('demo-guide,1,FAILED,1')
    }

    private File createStructuralFile(def project, String html) {
        File file = new File(project.buildDir, 'dist/guides/demo-guide/1/guide/single.html')
        file.parentFile.mkdirs()
        file.text = html.trim()
        file
    }
}

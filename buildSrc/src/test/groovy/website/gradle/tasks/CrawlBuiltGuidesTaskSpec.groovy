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

class CrawlBuiltGuidesTaskSpec extends Specification {

    @TempDir
    File tempDir

    def 'marks a guide version VERIFIED when local references resolve'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        File versionDir = createGuideVersion(project, '''
<html>
  <body>
    <a href="page.html#section">Page</a>
    <img src="images/logo.png" />
    <link href="styles/site.css" rel="stylesheet" />
    <script src="scripts/app.js"></script>
    <div id="home"></div>
  </body>
</html>
''')
        new File(versionDir, 'page.html').text = '<html><body><div id="section"></div></body></html>'
        new File(versionDir, 'images/logo.png').with { parentFile.mkdirs(); text = 'png' }
        new File(versionDir, 'styles/site.css').with { parentFile.mkdirs(); text = 'body {}' }
        new File(versionDir, 'scripts/app.js').with { parentFile.mkdirs(); text = 'console.log(1)' }
        CrawlBuiltGuidesTask.register(project)

        when:
        def task = project.tasks.getByName(CrawlBuiltGuidesTask.NAME) as CrawlBuiltGuidesTask
        task.crawl()

        then:
        task.reportFile.get().asFile.text.contains('demo-guide,1,VERIFIED,0')
    }

    def 'writes only the header when no guides are rendered yet'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        CrawlBuiltGuidesTask.register(project)

        when:
        def task = project.tasks.getByName(CrawlBuiltGuidesTask.NAME) as CrawlBuiltGuidesTask
        task.crawl()

        then:
        task.reportFile.get().asFile.readLines() == ['guide,version,status,issueCount,details']
    }

    def 'throws in hard-fail mode when broken files or anchors are found'() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        createGuideVersion(project, '''
<html>
  <body>
    <a href="#missing-anchor">Broken anchor</a>
    <img src="images/missing.png" />
  </body>
</html>
''')
        CrawlBuiltGuidesTask.register(project)
        def task = project.tasks.getByName(CrawlBuiltGuidesTask.NAME) as CrawlBuiltGuidesTask
        task.failOnViolation.set(true)

        when:
        task.crawl()

        then:
        def error = thrown(GradleException)
        error.message.contains('Broken rendered guide references detected')
        task.reportFile.get().asFile.text.contains('demo-guide,1,FAILED,2')
    }

    private File createGuideVersion(def project, String indexHtml) {
        File versionDir = new File(project.buildDir, 'dist/guides/demo-guide/1')
        versionDir.mkdirs()
        new File(versionDir, 'index.html').text = indexHtml.trim()
        versionDir
    }
}

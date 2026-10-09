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

class RecordToolReleaseTaskSpec extends Specification {

    @TempDir
    File tempDir

    private static final String BASELINE_YAML = '''\
companionArtifacts:
  '7':
    - artifactId: grails-quartz
      version: '4.0.1'
      mirrorDirectory: quartz
      releaseNotesRepo: apache/grails-quartz
      displayName: Grails Quartz Plugin

tools:
  # Same schema as companionArtifacts entries.
  - artifactId: grails-intellij-plugin
    version: '262.1.1'
    mirrorDirectory: intellij
    releaseNotesRepo: apache/grails-intellij-plugin
    displayName: Grails IntelliJ Plugin

coreReleases:
  - version: 7.0.0
'''

    private RecordToolReleaseTask registerTask(File yaml) {
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        RecordToolReleaseTask.register(project)
        def task = project.tasks.getByName(RecordToolReleaseTask.NAME) as RecordToolReleaseTask
        task.releasesYaml.set(yaml)
        return task
    }

    private File writeReleases(String content) {
        File f = new File(tempDir, 'releases.yml')
        f.text = content
        return f
    }

    void 'bumps version of an existing tool without touching surrounding lines'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.artifactId.set('grails-intellij-plugin')
            task.artifactVersion.set('262.1.2')

        when:
            task.recordToolRelease()

        then:
            yaml.text == BASELINE_YAML.replace("version: '262.1.1'", "version: '262.1.2'")
    }

    void 'bump ignores descriptor flags'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.artifactId.set('grails-intellij-plugin')
            task.artifactVersion.set('262.1.2')
            task.mirrorDirectory.set('somewhere-else')
            task.displayName.set('Renamed')

        when:
            task.recordToolRelease()

        then:
            yaml.text == BASELINE_YAML.replace("version: '262.1.1'", "version: '262.1.2'")
    }

    void 'does not bump a companion artifact with the same artifactId'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.artifactId.set('grails-quartz')
            task.artifactVersion.set('9.9.9')

        when:
            task.recordToolRelease()

        then: 'the tool is treated as new, and the companion entry is untouched'
            GradleException ex = thrown()
            ex.message.contains('is not yet listed under tools:')
            yaml.text == BASELINE_YAML
    }

    void 'appends a new tool at the end of tools: preserving the blank-line separator'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.artifactId.set('grails-vscode-extension')
            task.artifactVersion.set('1.0.0')
            task.mirrorDirectory.set('vscode')
            task.releaseNotesRepo.set('apache/grails-vscode-extension')
            task.displayName.set('Grails VS Code Extension')

        when:
            task.recordToolRelease()

        then:
            yaml.text == BASELINE_YAML.replace('''\
    displayName: Grails IntelliJ Plugin
''', '''\
    displayName: Grails IntelliJ Plugin
  - artifactId: grails-vscode-extension
    version: '1.0.0'
    mirrorDirectory: vscode
    releaseNotesRepo: apache/grails-vscode-extension
    displayName: Grails VS Code Extension
''')
    }

    void 'fails with a clear message when adding a new tool without descriptor flags'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.artifactId.set('grails-vscode-extension')
            task.artifactVersion.set('1.0.0')

        when:
            task.recordToolRelease()

        then:
            GradleException ex = thrown()
            ex.message.contains('-PmirrorDirectory')
            ex.message.contains('-PreleaseNotesRepo')
            ex.message.contains('-PdisplayName')
            yaml.text == BASELINE_YAML
    }

    void 'fails when releases.yml has no tools section'() {

        given:
            File yaml = writeReleases('''\
coreReleases:
  - version: 7.0.0
''')
            def task = registerTask(yaml)
            task.artifactId.set('grails-intellij-plugin')
            task.artifactVersion.set('262.1.2')

        when:
            task.recordToolRelease()

        then:
            GradleException ex = thrown()
            ex.message.contains('no tools: section')
    }

    void 'fails when artifactId or version is missing'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.artifactId.set('grails-intellij-plugin')
            task.artifactVersion.set('')

        when:
            task.recordToolRelease()

        then:
            GradleException ex = thrown()
            ex.message.contains('requires -PartifactId')
    }
}

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

class RecordCompanionReleaseTaskSpec extends Specification {

    @TempDir
    File tempDir

    private static final String BASELINE_YAML = '''\
companionArtifacts:
  '7':
    - artifactId: grails-spring-security
      version: '7.0.2'
      mirrorDirectory: spring-security
      releaseNotesRepo: apache/grails-spring-security
      displayName: Grails Spring Security Plugin
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin

coreReleases:
  - version: 7.0.0
'''

    private RecordCompanionReleaseTask registerTask(File yaml) {
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        RecordCompanionReleaseTask.register(project)
        def task = project.tasks.getByName(RecordCompanionReleaseTask.NAME) as RecordCompanionReleaseTask
        task.releasesYaml.set(yaml)
        return task
    }

    private File writeReleases(String content) {
        File f = new File(tempDir, 'releases.yml')
        f.text = content
        return f
    }

    void 'bumps version of an existing entry without touching surrounding lines'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('7')
            task.artifactId.set('grails-redis')
            task.artifactVersion.set('5.0.2')

        when:
            task.recordCompanionRelease()

        then:
            yaml.text == BASELINE_YAML.replace("version: '5.0.1'", "version: '5.0.2'")
    }

    void 'descriptor flags are silently ignored on the bump path'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('7')
            task.artifactId.set('grails-redis')
            task.artifactVersion.set('5.0.2')
            task.mirrorDirectory.set('SHOULD-NOT-APPEAR')
            task.releaseNotesRepo.set('SHOULD-NOT-APPEAR')
            task.displayName.set('SHOULD-NOT-APPEAR')

        when:
            task.recordCompanionRelease()

        then:
            !yaml.text.contains('SHOULD-NOT-APPEAR')
            yaml.text.contains("mirrorDirectory: redis")
    }

    void 'appends a new artifact entry to the end of an existing major block'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('7')
            task.artifactId.set('grails-quartz')
            task.artifactVersion.set('4.0.1')
            task.mirrorDirectory.set('quartz')
            task.releaseNotesRepo.set('apache/grails-quartz')
            task.displayName.set('Grails Quartz Plugin')

        when:
            task.recordCompanionRelease()

        then:
            yaml.text == '''\
companionArtifacts:
  '7':
    - artifactId: grails-spring-security
      version: '7.0.2'
      mirrorDirectory: spring-security
      releaseNotesRepo: apache/grails-spring-security
      displayName: Grails Spring Security Plugin
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin
    - artifactId: grails-quartz
      version: '4.0.1'
      mirrorDirectory: quartz
      releaseNotesRepo: apache/grails-quartz
      displayName: Grails Quartz Plugin

coreReleases:
  - version: 7.0.0
'''
    }

    void 'bootstraps a brand-new major block at the end of companionArtifacts: preserving the blank-line separator'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('8')
            task.artifactId.set('grails-publish')
            task.artifactVersion.set('1.0.0-M1')
            task.mirrorDirectory.set('grails-publish')
            task.releaseNotesRepo.set('apache/grails-gradle-publish')
            task.displayName.set('Grails Publish Gradle Plugin')

        when:
            task.recordCompanionRelease()

        then:
            yaml.text == '''\
companionArtifacts:
  '7':
    - artifactId: grails-spring-security
      version: '7.0.2'
      mirrorDirectory: spring-security
      releaseNotesRepo: apache/grails-spring-security
      displayName: Grails Spring Security Plugin
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin
  '8':
    - artifactId: grails-publish
      version: '1.0.0-M1'
      mirrorDirectory: grails-publish
      releaseNotesRepo: apache/grails-gradle-publish
      displayName: Grails Publish Gradle Plugin

coreReleases:
  - version: 7.0.0
'''
    }

    void 'fails with a clear message when bootstrapping a new major without descriptor flags'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('8')
            task.artifactId.set('grails-publish')
            task.artifactVersion.set('1.0.0-M1')

        when:
            task.recordCompanionRelease()

        then:
            GradleException ex = thrown()
            ex.message.contains("no '8': block exists yet")
            ex.message.contains('-PmirrorDirectory')
            ex.message.contains('-PreleaseNotesRepo')
            ex.message.contains('-PdisplayName')
    }

    void 'fails with a clear message when adding a new artifact to an existing major without descriptor flags'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('7')
            task.artifactId.set('grails-quartz')
            task.artifactVersion.set('4.0.1')

        when:
            task.recordCompanionRelease()

        then:
            GradleException ex = thrown()
            ex.message.contains('artifactId=grails-quartz is not yet listed')
            ex.message.contains("under '7':")
            ex.message.contains('-PmirrorDirectory')
    }

    void 'partial descriptor flags still trigger the missing-flags error listing only the missing ones'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('8')
            task.artifactId.set('grails-publish')
            task.artifactVersion.set('1.0.0-M1')
            task.mirrorDirectory.set('grails-publish')

        when:
            task.recordCompanionRelease()

        then:
            GradleException ex = thrown()
            !ex.message.contains('-PmirrorDirectory')
            ex.message.contains('-PreleaseNotesRepo')
            ex.message.contains('-PdisplayName')
    }

    void 'fails when releases.yml has no companionArtifacts section at all'() {

        given:
            File yaml = writeReleases('coreReleases:\n  - version: 7.0.0\n')
            def task = registerTask(yaml)
            task.grailsMajor.set('7')
            task.artifactId.set('grails-redis')
            task.artifactVersion.set('5.0.2')

        when:
            task.recordCompanionRelease()

        then:
            GradleException ex = thrown()
            ex.message.contains('no companionArtifacts: section')
    }

    void 'fails when required core flags are missing'() {

        given:
            File yaml = writeReleases(BASELINE_YAML)
            def task = registerTask(yaml)
            task.grailsMajor.set('')
            task.artifactId.set('grails-redis')
            task.artifactVersion.set('5.0.2')

        when:
            task.recordCompanionRelease()

        then:
            GradleException ex = thrown()
            ex.message.contains('-PgrailsMajor')
            ex.message.contains('-PartifactId')
            ex.message.contains('-PartifactVersion')
    }

    void 'preserves leading comments inside companionArtifacts: when bumping'() {

        given:
            String yamlWithComments = '''\
companionArtifacts:
  # leading comment that must survive
  '7':
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin

coreReleases:
  - version: 7.0.0
'''
            File yaml = writeReleases(yamlWithComments)
            def task = registerTask(yaml)
            task.grailsMajor.set('7')
            task.artifactId.set('grails-redis')
            task.artifactVersion.set('5.0.2')

        when:
            task.recordCompanionRelease()

        then:
            yaml.text.contains('# leading comment that must survive')
            yaml.text.contains("version: '5.0.2'")
            !yaml.text.contains("version: '5.0.1'")
    }

    void 'inserts a new entry between an existing major and the next major when both already exist'() {

        given:
            String yamlMultiMajor = '''\
companionArtifacts:
  '7':
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin
  '8':
    - artifactId: grails-publish
      version: '1.0.0-M1'
      mirrorDirectory: grails-publish
      releaseNotesRepo: apache/grails-gradle-publish
      displayName: Grails Publish Gradle Plugin

coreReleases:
  - version: 7.0.0
'''
            File yaml = writeReleases(yamlMultiMajor)
            def task = registerTask(yaml)
            task.grailsMajor.set('7')
            task.artifactId.set('grails-quartz')
            task.artifactVersion.set('4.0.1')
            task.mirrorDirectory.set('quartz')
            task.releaseNotesRepo.set('apache/grails-quartz')
            task.displayName.set('Grails Quartz Plugin')

        when:
            task.recordCompanionRelease()

        then:
            yaml.text == '''\
companionArtifacts:
  '7':
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin
    - artifactId: grails-quartz
      version: '4.0.1'
      mirrorDirectory: quartz
      releaseNotesRepo: apache/grails-quartz
      displayName: Grails Quartz Plugin
  '8':
    - artifactId: grails-publish
      version: '1.0.0-M1'
      mirrorDirectory: grails-publish
      releaseNotesRepo: apache/grails-gradle-publish
      displayName: Grails Publish Gradle Plugin

coreReleases:
  - version: 7.0.0
'''
    }
}

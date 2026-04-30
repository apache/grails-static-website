/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir

/**
 * Behaviour spec for {@link ValidateGuidesTask}. Exercises shape-mode
 * pass/fail paths, existence-mode SKIP-WARN behaviour for not-yet-migrated
 * guides, and the unknown-mode failure path.
 */
class ValidateGuidesTaskSpec extends Specification {

    @TempDir
    File tempDir

    def 'task is registered with the migration group and a description'() {
        when:
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        ValidateGuidesTask.register(project)

        then:
        ValidateGuidesTask task = project.tasks.findByName(ValidateGuidesTask.NAME) as ValidateGuidesTask
        task != null
        task.group == ValidateGuidesTask.GROUP
        task.description.contains('-PvalidationMode=')
    }

    def 'shape mode passes for a minimal valid guides.yml'() {
        given:
        File yml = newGuidesYml(VALID_MINIMAL)

        expect:
        runValidate(yml, ValidateGuidesTask.MODE_SHAPE)
    }

    def 'shape mode rejects a non-40-char SHA'() {
        given:
        File yml = newGuidesYml(VALID_MINIMAL.replace(
                '0123456789abcdef0123456789abcdef01234567',
                'short-sha'))

        when:
        runValidate(yml, ValidateGuidesTask.MODE_SHAPE)

        then:
        GradleException e = thrown()
        e.message.contains('1 error')
    }

    def 'shape mode rejects a non-ISO-8601 publicationDate'() {
        given:
        File yml = newGuidesYml(VALID_MINIMAL.replace(
                "publicationDate: '2020-01-15'",
                "publicationDate: '15/01/2020'"))

        when:
        runValidate(yml, ValidateGuidesTask.MODE_SHAPE)

        then:
        GradleException e = thrown()
        e.message.contains('error')
    }

    def 'shape mode rejects duplicate guide names'() {
        given:
        File yml = newGuidesYml(VALID_MINIMAL + '''
  - name: my-guide
    title: 'Duplicate'
    category: 'Getting Started'
    publicationDate: '2020-01-15'
    versions:
      '6':
        sourcePath: guides/my-guide-2/v6
''')

        when:
        runValidate(yml, ValidateGuidesTask.MODE_SHAPE)

        then:
        // The detailed error ("duplicate name 'my-guide'") is logged at error level;
        // the GradleException carries only the count summary.
        GradleException e = thrown()
        e.message.contains('1 error')
    }

    def 'shape mode rejects extends: shared without a shared block on the parent'() {
        given:
        File yml = newGuidesYml(VALID_MINIMAL.replace(
                "sourcePath: guides/my-guide/v6",
                "sourcePath: guides/my-guide/v6\n        extends: shared"))

        when:
        runValidate(yml, ValidateGuidesTask.MODE_SHAPE)

        then:
        GradleException e = thrown()
        e.message.contains('error')
    }

    def 'existence mode SKIP-WARNs entries whose sourcePath does not exist on disk'() {
        given:
        File yml = newGuidesYml(VALID_MINIMAL)

        expect:
        // SKIPs are warnings, not failures, so the task succeeds with 0 errors.
        runValidate(yml, ValidateGuidesTask.MODE_EXISTENCE)
    }

    def 'unknown mode fails with a clear error message'() {
        given:
        File yml = newGuidesYml(VALID_MINIMAL)

        when:
        runValidate(yml, 'gibberish')

        then:
        GradleException e = thrown()
        e.message.contains("Unknown validationMode 'gibberish'")
    }

    private boolean runValidate(File yml, String mode) {
        Project project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        ValidateGuidesTask.register(project)
        ValidateGuidesTask task = project.tasks.getByName(ValidateGuidesTask.NAME) as ValidateGuidesTask
        task.guidesYml.set(yml)
        task.mode.set(mode)
        // projectRoot is already conventioned to project.layout.projectDirectory,
        // which is tempDir per ProjectBuilder.withProjectDir(). Do not re-set.
        task.validate()
        return true
    }

    private File newGuidesYml(String contents) {
        File f = new File(tempDir, 'conf-guides-' + System.nanoTime() + '.yml')
        f.parentFile.mkdirs()
        f.text = contents
        return f
    }

    /**
     * Minimal guides.yml that passes shape-mode validation.
     */
    private static final String VALID_MINIMAL = '''
defaults:
  category: 'Getting Started'
  authors: []
  tags: []

guides:
  - name: my-guide
    title: 'My Guide'
    subtitle: 'Sub'
    authors:
      - 'Test Author'
    category: 'Getting Started'
    publicationDate: '2020-01-15'
    versions:
      '6':
        sourcePath: guides/my-guide/v6
        publicationDate: '2020-01-15'
        tags: [tag1]
        sampleRef:
          repo: example/my-guide
          branch: master
          sha: '0123456789abcdef0123456789abcdef01234567'
'''
}

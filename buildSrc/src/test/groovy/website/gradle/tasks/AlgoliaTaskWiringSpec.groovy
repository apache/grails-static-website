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

import website.gradle.RenderGuidesPlugin

import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir

class AlgoliaTaskWiringSpec extends Specification {

    @TempDir
    File tempDir

    def 'registers the export pipeline and its required dependencies'() {
        given:
        new File(tempDir, 'conf').mkdirs()
        new File(tempDir, 'conf/guides.yml').text = 'guides: []\n'
        def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        project.pluginManager.apply('grails-website')

        when:
        def exportTask = project.tasks.getByName(ExportAlgoliaIndexTask.NAME)
        def uploadTask = project.tasks.getByName(UploadAlgoliaIndexTask.NAME)
        def buildTask = project.tasks.getByName('build')

        then:
        exportTask.taskDependencies.getDependencies(exportTask)*.name.containsAll([
                'build', 'buildGuides', 'buildAllGuides',
                CollectAlgoliaDocumentationTask.NAME
        ])
        uploadTask.taskDependencies.getDependencies(uploadTask)*.name.contains(ExportAlgoliaIndexTask.NAME)
        buildTask.taskDependencies.getDependencies(buildTask)*.name.containsAll([
                'buildGuides', RenderGuidesPlugin.AGGREGATE_TASK
        ])
    }
}

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
package org.grails.gradle.tasks

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.grails.documentation.DocumentationPage
import org.grails.gradle.GrailsWebsiteExtension

@CompileStatic
@CacheableTask
class DocumentationTask extends GrailsWebsiteTask {

    @Internal
    String description = 'Generates the Documentation HTML Page -> build/temp/documentation.html'

    public static final String NAME = 'genDocsPage'

    private final ObjectFactory objects

    @Inject
    DocumentationTask(ObjectFactory objects) {
        this.objects = objects
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty modules = objects.fileProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty releases = objects.fileProperty()

    @Input
    final Property<String> url = objects.property(String)

    @OutputDirectory
    final DirectoryProperty outputDir = objects.directoryProperty()

    static TaskProvider<DocumentationTask> register(Project project, GrailsWebsiteExtension siteExt, String name = NAME) {
        project.tasks.register(name, DocumentationTask) {
            it.modules.set(siteExt.modules)
            it.url.set(siteExt.url)
            it.releases.set(siteExt.releases)
            it.outputDir.set(siteExt.outputDir)
        }
    }

    @TaskAction
    void renderDocsPage() {
        def buildDir = outputDir.get().asFile
        def tempDir = new File(buildDir, 'temp').tap {mkdir() }
        def outputFile = new File(tempDir, 'documentation.html').tap { createNewFile() }
        outputFile.text =
                'title: Documentation | Grails Framework\n' +
                'body: docs\n' +
                '---\n' +
                DocumentationPage.mainContent(
                        releases.get().asFile,
                        modules.get().asFile
                )
    }
}

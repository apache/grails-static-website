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

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import website.gradle.GrailsWebsiteExtension
import website.model.questions.QuestionsPage

@CompileStatic
@CacheableTask
class QuestionsTask extends GrailsWebsiteTask {

    @Internal
    final String description = 'Generates FAQ HTML - build/temp/faq.html'

    public static final String NAME = 'genFaq'

    private static final String PAGE_NAME_QUESTIONS = 'faq.html'

    private final ObjectFactory objects

    @Inject
    QuestionsTask(ObjectFactory objects) {
        this.objects = objects
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty questions = objects.fileProperty()

    @OutputDirectory
    final DirectoryProperty outputDir = objects.directoryProperty()

    static TaskProvider<QuestionsTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, QuestionsTask) {
            it.outputDir.set(siteExt.outputDir)
            it.questions.set(siteExt.questions)
        }
    }

    @TaskAction
    void renderQuestionsPage() {
        def buildDir = outputDir.get().asFile
        def temp = new File(buildDir, 'temp').tap { it.mkdirs() }
        def output = new File(temp, PAGE_NAME_QUESTIONS)
        output.setText(
                "title: FAQ | Apache Grails&reg;\nbody: faq\n---\n" + QuestionsPage.mainContent(questions.get().asFile),
                'UTF-8'
        )
    }
}

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

import website.model.documentation.ProfilesPage
import website.gradle.GrailsWebsiteExtension

@CompileStatic
@CacheableTask
class ProfilesTask extends GrailsWebsiteTask {

    @Internal
    final String description = 'Generates the Profiles HTML Page -> build/temp/profiles.html'

    public static final String NAME = 'genProfilesPage'

    private final ObjectFactory objects

    @Inject
    ProfilesTask(ObjectFactory objects) {
        this.objects = objects
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty profiles = objects.fileProperty()

    @OutputDirectory
    final DirectoryProperty outputDir = objects.directoryProperty()

    static TaskProvider<ProfilesTask> register(Project project, GrailsWebsiteExtension siteExt) {
        project.tasks.register(NAME, ProfilesTask) {
            it.profiles.set(siteExt.profiles)
            it.outputDir.set(siteExt.outputDir)
        }
    }

    @TaskAction
    void renderProfilesPage() {
        def tempDir = new File(outputDir.get().asFile, 'temp').tap { mkdirs() }
        def output = new File(tempDir, 'profiles.html')
        output.setText(
                'title: Profiles | Grails Framework\n' +
                        'body: docs\n' +
                        '---\n' +
                        ProfilesPage.mainContent(profiles.get().asFile),
                'UTF-8'
        )
    }
}

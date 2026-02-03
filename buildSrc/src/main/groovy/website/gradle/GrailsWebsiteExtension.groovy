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
package website.gradle

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory

@CompileStatic
class GrailsWebsiteExtension {

    public static final String NAME = 'grailsWebsite'

    final RegularFileProperty htaccess
    final RegularFileProperty modules
    final RegularFileProperty profiles
    final RegularFileProperty questions
    final RegularFileProperty releases
    final RegularFileProperty template

    final DirectoryProperty assetsDir
    final DirectoryProperty minutesDir
    final DirectoryProperty outputDir
    final DirectoryProperty pagesDir
    final DirectoryProperty postsDir

    final Property<String> description
    final Property<String> robots
    final Property<String> title
    final Property<String> url

    final ListProperty<String> keywords

    @Inject
    GrailsWebsiteExtension(ObjectFactory objects, ProjectLayout layout, ProviderFactory providers) {
        // File properties with conventions
        htaccess = objects.fileProperty()
        htaccess.convention(layout.buildDirectory.file('dist/.htaccess'))

        modules = objects.fileProperty()
        modules.convention(layout.projectDirectory.file('conf/modules.yml'))

        profiles = objects.fileProperty()
        profiles.convention(layout.projectDirectory.file('conf/profiles.yml'))

        questions = objects.fileProperty()
        questions.convention(layout.projectDirectory.file('conf/questions.yml'))

        releases = objects.fileProperty()
        releases.convention(layout.projectDirectory.file('conf/releases.yml'))

        template = objects.fileProperty()
        template.convention(layout.projectDirectory.file('templates/document.html'))

        // Directory properties with conventions
        assetsDir = objects.directoryProperty()
        assetsDir.convention(layout.projectDirectory.dir('assets'))

        minutesDir = objects.directoryProperty()
        minutesDir.convention(layout.projectDirectory.dir('minutes'))

        outputDir = objects.directoryProperty()
        outputDir.convention(layout.buildDirectory)

        pagesDir = objects.directoryProperty()
        pagesDir.convention(layout.projectDirectory.dir('pages'))

        postsDir = objects.directoryProperty()
        postsDir.convention(layout.projectDirectory.dir('posts'))

        // String properties with conventions
        description = objects.property(String)
        description.convention('A powerful Groovy-based web application framework for the JVM built on top of Spring Boot')

        robots = objects.property(String)
        robots.convention('all')

        title = objects.property(String)
        title.convention('Grails Framework')

        url = objects.property(String)
        url.convention(
            providers.environmentVariable('GRAILS_WS_URL')
                .orElse('https://grails.apache.org')
        )

        // List property with convention
        keywords = objects.listProperty(String)
        keywords.convention(['grails', 'jvm', 'framework', 'groovy', 'gradle', 'spring-boot', 'gorm'])
    }
}

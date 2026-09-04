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

import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir

class GenerateAlgoliaConfigTaskSpec extends Specification {

    @TempDir
    File tempDir

    def 'generates only the public browser configuration'() {
        given: 'a Gradle project with the GenerateAlgoliaConfigTask configured with only the public application ID and search API key'
            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            def task = GenerateAlgoliaConfigTask.register(project).get()
            task.applicationId.set('APP123')
            task.searchApiKey.set('search-key')

        when: 'the task is executed'
            task.generate()

        then: 'the output file contains only the public browser configuration'
            task.outputFile.get().asFile.text ==
                    'window.GRAILS_ALGOLIA_CONFIG = { appId: "APP123", searchApiKey: "search-key", indexName: "grails_site_search" };\n'
            !task.outputFile.get().asFile.text.containsIgnoreCase('ADMIN')
    }
}

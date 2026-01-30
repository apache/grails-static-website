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
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.grails.gradle.GrailsWebsiteExtension

@CompileStatic
@CacheableTask
class HtaccessTask extends GrailsWebsiteTask {

    @Internal
    String description = 'Generates the .htaccess file'

    public static final String NAME = 'genHtaccess'

    private static final List<String> DOMAINS = [
            'https://*.kapa.ai/',
            'https://kapa-widget-proxy-la7dkmplpq-uc.a.run.app',
            'https://www.google.com/recaptcha/',
            'https://www.gstatic.com/recaptcha/',
            'https://hcaptcha.com',
            'https://*.hcaptcha.com'
        ]

    private static String HT_ACCESS_CONTENT =
            '# Custom 404 error page\n' +
            'ErrorDocument 404 /404.html\n' +
            '\n' +
            '# CSP permissions for grails.apache.org - https://issues.apache.org/jira/browse/INFRA-27297\n' +
            '# Ref https://docs.kapa.ai/integrations/understanding-csp-cors\n' +
            'SetEnv CSP_PROJECT_DOMAINS "' + DOMAINS.join(' ') + '"'

    private final ObjectFactory objects

    @Inject
    HtaccessTask(ObjectFactory objects) {
        this.objects = objects
    }

    @Internal
    final DirectoryProperty outputDir = objects.directoryProperty()

    @OutputFile
    final RegularFileProperty htaccessFile = objects.fileProperty()

    static TaskProvider<HtaccessTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, HtaccessTask) {
            it.outputDir.set(siteExt.outputDir)
            it.htaccessFile.convention(
                    project.layout.buildDirectory.file('dist/.htaccess')
            )
        }
    }

    @TaskAction
    void generateHtaccess() {
        def outputDir = new File(outputDir.get().asFile, 'dist').tap {mkdirs() }
        def htaccess = new File(outputDir, '.htaccess').tap {
            text = HT_ACCESS_CONTENT
        }
        logger.lifecycle('Generated .htaccess file at: {}', htaccess.absolutePath)
    }
}

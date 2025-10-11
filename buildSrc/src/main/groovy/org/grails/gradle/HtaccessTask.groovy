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
package org.grails.gradle

import groovy.transform.CompileStatic

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CompileStatic
class HtaccessTask extends DefaultTask {

    private static final List<String> DOMAINS = [
            'https://*.kapa.ai/',
            'https://kapa-widget-proxy-la7dkmplpq-uc.a.run.app',
            'https://www.google.com/recaptcha/',
            'https://www.gstatic.com/recaptcha/',
            'https://hcaptcha.com',
            'https://*.hcaptcha.com'
        ]

    private static String HT_ACCESS_CONTENT =
            '# CSP permissions for grails.apache.org - https://issues.apache.org/jira/browse/INFRA-27297\n' +
            '# Ref https://docs.kapa.ai/integrations/understanding-csp-cors\n' +
            'SetEnv CSP_PROJECT_DOMAINS "' + DOMAINS.join(' ') + '"'

    @Input
    final Property<File> output = project.objects.property(File)

    @OutputFile
    final RegularFileProperty htaccessFile = project.objects.fileProperty()

    HtaccessTask() {
        htaccessFile.convention(
                project.layout.buildDirectory.file('dist/.htaccess')
        )
    }

    @TaskAction
    void generateHtaccess() {
        def outputDir = new File(output.get(), 'dist').tap {
            mkdirs()
        }
        def htaccess = new File(outputDir, '.htaccess').tap {
            text = HT_ACCESS_CONTENT
        }
        logger.lifecycle("Generated .htaccess file at: $htaccess.absolutePath")
    }
}

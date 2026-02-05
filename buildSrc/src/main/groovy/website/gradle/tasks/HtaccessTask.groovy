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

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import website.gradle.GrailsWebsiteExtension

@CompileStatic
@CacheableTask
abstract class HtaccessTask extends GrailsWebsiteTask {

    @Internal
    final String description = 'Generates the .htaccess file'

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

    @OutputFile
    abstract RegularFileProperty getHtaccess()

    static TaskProvider<HtaccessTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, HtaccessTask) {
            it.htaccess.set(siteExt.htaccess)
        }
    }

    @TaskAction
    void generateHtaccess() {
        htaccess.get().asFile.text = HT_ACCESS_CONTENT
    }
}

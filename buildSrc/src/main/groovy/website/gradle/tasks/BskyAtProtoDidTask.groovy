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
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.yaml.snakeyaml.Yaml
import website.gradle.GrailsWebsiteExtension

@CompileStatic
@CacheableTask
abstract class BskyAtProtoDidTask extends GrailsWebsiteTask {

    @Internal
    final String description = 'Generates the .well-known/atproto-did file for Bsky'

    public static final String NAME = 'genBskyAtProtoDid'

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getSocial()

    @OutputFile
    abstract RegularFileProperty getBskyAtProtoDid()

    static TaskProvider<BskyAtProtoDidTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, BskyAtProtoDidTask) {
            it.social.set(siteExt.social)
            it.bskyAtProtoDid.set(siteExt.bskyAtProtoDid)
        }
    }

    @TaskAction
    void generateAtProtoDid() {
        bskyAtProtoDid.get().asFile.tap {
            parentFile.mkdirs()
            text = getBskyAtProtoDidText() 
        }
    }

    private Map getSocialProperties() {
        File socialFile = social.get().asFile
        new Yaml().load(socialFile.newDataInputStream()) as Map
    }
    
    private String getBskyAtProtoDidText() {
        socialProperties['bsky']['did']
    }
}

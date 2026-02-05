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
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import website.gradle.GrailsWebsiteExtension

@CompileStatic
@CacheableTask
abstract class AssetsTask extends GrailsWebsiteTask {

    @Internal
    final String description =
            'Copies css, js, fonts and images from the assets folder to the dist folder'

    public static final String NAME = 'copyAssets'

    // dir name -> list of file name glob filters
    static final Map<String,List<String>> assetTypes = [
            'files': ['*.jar', '*.md5', '*.pdf'],
            'fonts': ['*.eot', '*.ttf', '*.woff', '*.woff2'],
            'images': ['*.gif', '*.ico', '*.jpeg', '*.jpg', '*.png', '*.svg'],
            'javascripts': ['*.js'],
            'stylesheets': ['*.css'],
    ]

    @Inject
    abstract FileSystemOperations getFileSystemOperations()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getAssetsDir()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    static TaskProvider<AssetsTask> register(Project project, GrailsWebsiteExtension siteExt) {
        project.tasks.register(NAME, AssetsTask) {
            it.outputDir.set(siteExt.outputDir)
            it.assetsDir.set(siteExt.assetsDir)
        }
    }

    @TaskAction
    void copyAssets() {
        assetTypes.each { dirName, extFilters ->
            fileSystemOperations.copy { CopySpec cs ->
                cs.from(assetsDir.dir(dirName))
                cs.into(outputDir.dir("dist/$dirName"))
                cs.include(extFilters.collect { '**/' + it })
                cs.includeEmptyDirs = false
            }
        }
    }
}

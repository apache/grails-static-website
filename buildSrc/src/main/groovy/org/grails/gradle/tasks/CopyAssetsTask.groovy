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
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.grails.gradle.GrailsWebsiteExtension

@CompileStatic
@CacheableTask
class CopyAssetsTask extends GrailsWebsiteTask {

    @Internal
    String description = 'Copies css, js, fonts and images from the assets folder to the dist folder'

    public static final String NAME = 'copyAssets'

    public static final List<String> FONT_EXTENSIONS = ['*.eot', '*.ttf', '*.woff', '*.woff2']
    public static final List<String> JAVASCRIPT_EXTENSIONS = ['*.js']
    public static final List<String> CSS_EXTENSIONS = ['*.css']
    public static final List<String> IMAGE_EXTENSIONS = ['*.ico', '*.png', '*.svg', '*.jpg', '*.jpeg', '*.gif']
    public static final List<String> FILE_EXTENSIONS = ['*.jar', '*.md5', '*.pdf']

    private final ObjectFactory objects

    @Inject
    CopyAssetsTask(ObjectFactory objects) {
        this.objects = objects
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty assetsDir = objects.directoryProperty()

    @OutputDirectory
    final DirectoryProperty outputDir = objects.directoryProperty()

    @TaskAction
    void copyAssets() {
        copyImages()
        copyCss()
        copyJs()
        copyFonts()
        copyFiles()
    }

    static TaskProvider<CopyAssetsTask> register(Project project, GrailsWebsiteExtension siteExt) {
        project.tasks.register(NAME, CopyAssetsTask) {
            it.outputDir.set(siteExt.outputDir)
            it.assetsDir.set(siteExt.assetsDir)
        }
    }

    private File getDistDir() {
        new File(outputDir.get().asFile, 'dist').tap { it.mkdirs() }
    }

    static List<String> recursiveIncludes(Iterable<String> extensions) {
        extensions.collect {
            ["$it", "**/$it"]
        }.flatten() as List<String>
    }

    void copyImages() {
        def srcDir = new File(assetsDir.get().asFile, 'images')
        def destDir = new File(distDir, 'images').tap { it.mkdirs() }
        project.copy { CopySpec copySpec ->
            copySpec.from(srcDir)
            copySpec.into(destDir)
            copySpec.include(recursiveIncludes(IMAGE_EXTENSIONS))
            copySpec.setIncludeEmptyDirs(false)
        }
    }

    void copyCss() {
        def cssSrcDir = new File(assetsDir.get().asFile, 'stylesheets')
        def cssDstDir = new File(distDir, 'stylesheets').tap {it.mkdirs() }
        project.copy { CopySpec copySpec ->
            copySpec.from(cssSrcDir)
            copySpec.into(cssDstDir)
            copySpec.include(CSS_EXTENSIONS)
        }
    }

    void copyFonts() {
        def dstFontsDir = new File(distDir, 'fonts').tap { it.mkdirs() }
        def srcFontsDir = new File(assetsDir.get().asFile, 'fonts')
        project.copy { CopySpec copySpec ->
            copySpec.from(srcFontsDir)
            copySpec.into(dstFontsDir)
            copySpec.include(FONT_EXTENSIONS)
        }
    }

    void copyJs() {
        def jsDstDir = new File(distDir, 'javascripts').tap { it.mkdirs() }
        def jsSrcDir = new File(assetsDir.get().asFile, 'javascripts')
        project.copy { CopySpec copySpec ->
            copySpec.from(jsSrcDir)
            copySpec.into(jsDstDir)
            copySpec.include(JAVASCRIPT_EXTENSIONS)
        }
    }

    void copyFiles() {
        def filesDstDir = new File(distDir, 'files').tap { it.mkdirs() }
        def filesSrcDir = new File(assetsDir.get().asFile, 'files')
        project.copy { CopySpec copySpec ->
            copySpec.from(filesSrcDir)
            copySpec.into(filesDstDir)
            copySpec.include(recursiveIncludes(FILE_EXTENSIONS))
        }
    }
}

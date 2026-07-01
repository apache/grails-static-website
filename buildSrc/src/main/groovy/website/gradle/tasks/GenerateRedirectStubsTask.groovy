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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.yaml.snakeyaml.Yaml

/**
 * Generates meta-refresh redirect stubs that map every URL on the legacy
 * {@code https://guides.grails.org} site to its new canonical location
 * under {@code https://grails.apache.org/guides/}. The stubs are intended
 * to be force-pushed to the {@code grails-guides-template} gh-pages branch
 * so legacy bookmarks and search-engine results keep working after the
 * cutover.
 */
@CompileStatic
abstract class GenerateRedirectStubsTask extends DefaultTask {

    static final String NAME = 'generateRedirectStubs'
    static final String GROUP = 'migration'
    static final String NEW_BASE = 'https://grails.apache.org/guides'

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getGuidesYml()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLegacyPaths()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    @TaskAction
    @CompileDynamic
    void generate() {
        File yml = guidesYml.get().asFile
        File outRoot = outputDir.get().asFile
        outRoot.deleteDir()
        outRoot.mkdirs()

        Map root = yml.withReader('UTF-8') { new Yaml().load(it) as Map }
        List guides = root.guides as List ?: []

        // Build a name -> latest-version map so the catch-all loop can resolve
        // the canonical destination for any per-guide legacy URL.
        Map<String, String> latestByName = [:]
        for (Map g : guides as List<Map>) {
            String name = g.name as String
            Map versions = g.versions as Map ?: [:]
            if (name && !versions.isEmpty()) {
                latestByName[name] = (versions.keySet() as List).sort().last() as String
            }
        }

        int stubs = 0

        // Per-guide canonical entry points, plus per-version URL prefix used
        // by legacy multi-version layout.
        for (Map g : guides as List<Map>) {
            String name = g.name as String
            Map versions = g.versions as Map ?: [:]
            if (!name || versions.isEmpty()) continue
            String latest = latestByName[name]

            stubs += writeStub(outRoot, "${name}/guide/index.html",
                    "${NEW_BASE}/${name}/${latest}/guide/index.html".toString())
            stubs += writeStub(outRoot, "${name}/guide/single.html",
                    "${NEW_BASE}/${name}/${latest}/guide/index.html".toString())
            stubs += writeStub(outRoot, "${name}/index.html",
                    "${NEW_BASE}/${name}/${latest}/guide/index.html".toString())

            for (Object versionKeyObj : versions.keySet()) {
                String v = versionKeyObj as String
                String slugAlt = (g.githubSlug as String)?.replace('grails-guides/', '')
                for (String slug : [name, slugAlt].findAll { it }) {
                    stubs += writeStub(outRoot, "grails${v}/${slug}/guide/index.html".toString(),
                            "${NEW_BASE}/${name}/${v}/guide/index.html".toString())
                }
            }
        }

        // Walk the captured legacy URL list and emit stubs for tag/category
        // pages plus any per-chapter HTML the per-guide loop did not cover.
        File legacy = legacyPaths.get().asFile
        if (legacy.isFile()) {
            legacy.eachLine('UTF-8') { String raw ->
                String path = raw?.trim()
                if (!path || !path.endsWith('.html')) return
                if (new File(outRoot, path).isFile()) return  // already written
                String target = GenerateRedirectStubsTask.mapLegacyPath(path, latestByName)
                if (target) {
                    stubs += GenerateRedirectStubsTask.writeStub(outRoot, path, target)
                }
            }
        }

        stubs += writeStub(outRoot, 'index.html', "${NEW_BASE}/index.html".toString())
        stubs += writeStub(outRoot, '404.html', "${NEW_BASE}/index.html".toString())

        logger.lifecycle("Wrote ${stubs} redirect stubs under ${outRoot}.")
    }

    @CompileStatic
    private static String mapLegacyPath(String path, Map<String, String> latestByName) {
        if (path.startsWith('tags/')) {
            return "${NEW_BASE}/${path}".toString()
        }
        if (path.startsWith('categories/')) {
            return "${NEW_BASE}/${path}".toString()
        }
        // Per-guide chapter pages: /<guide>/<chapter>.html or /<guide>/guide/pages/<chapter>.html
        // map to the single-page rendering at /<guide>/<latest>/guide/index.html.
        String[] parts = path.split('/')
        if (parts.length >= 1) {
            String guideName = parts[0]
            String latest = latestByName[guideName]
            if (latest) {
                return "${NEW_BASE}/${guideName}/${latest}/guide/index.html".toString()
            }
        }
        return null
    }

    @CompileStatic
    private static int writeStub(File outRoot, String relPath, String target) {
        File dest = new File(outRoot, relPath)
        dest.parentFile.mkdirs()
        dest.setText("""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Redirecting to ${target}</title>
<link rel="canonical" href="${target}">
<meta http-equiv="refresh" content="0; url=${target}">
<meta name="robots" content="noindex">
</head>
<body>
<p>This page has moved. <a href="${target}">${target}</a></p>
<script>window.location.replace("${target}");</script>
</body>
</html>
""", 'UTF-8')
        return 1
    }

    static TaskProvider<GenerateRedirectStubsTask> register(Project project) {
        project.tasks.register(NAME, GenerateRedirectStubsTask) { task ->
            task.group = GROUP
            task.description =
                    'Generates meta-refresh redirect stubs that map legacy guides.grails.org URLs to grails.apache.org/guides.'
            task.guidesYml.set(project.rootProject.layout.projectDirectory.file('conf/guides.yml'))
            task.legacyPaths.set(project.rootProject.layout.projectDirectory.file('conf/legacy-guide-paths.txt'))
            task.outputDir.set(project.layout.buildDirectory.dir('redirects'))
        }
    }
}

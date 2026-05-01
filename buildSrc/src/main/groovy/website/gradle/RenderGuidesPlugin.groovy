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
package website.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.yaml.snakeyaml.Yaml

import grails.doc.gradle.PublishGuideTask

/**
 * Wires guide-rendering tasks onto a Gradle project.
 *
 * <p>For each {@code (guide, version)} pair in {@code conf/guides.yml} whose
 * {@code versions[v].sourcePath/guide/} directory exists on disk, this
 * extension registers a {@link PublishGuideTask} named
 * {@code renderGuide_<sanitizedGuideName>_<version>}. Versions that point
 * at directories not yet on disk are silently skipped, so the build stays
 * green during the bulk-migration phase before all 124 guides have been
 * vendored.</p>
 *
 * <p>An aggregate {@code buildAllGuides} task depends on every per-version
 * task, so the conventional invocation is
 * {@code ./gradlew buildAllGuides}.</p>
 *
 * <p>Per-version AsciiDoc attributes are sourced from the matching
 * {@code <sourcePath>/manifest.yml} file (every key becomes an attribute),
 * with sensible fallbacks pulled from the parent {@code conf/guides.yml}
 * entry when {@code manifest.yml} omits a field.</p>
 *
 * <p>The renderer itself is the vendored {@code grails.doc.*} subtree (see
 * {@code buildSrc/VENDOR.md}). Templates and theme assets live in
 * {@code buildSrc/src/main/template/} and are passed to
 * {@link PublishGuideTask#getResourcesDir()} as a filesystem path.</p>
 */
@CompileStatic
class RenderGuidesPlugin {

    static final String GROUP = 'documentation'
    static final String AGGREGATE_TASK = 'buildAllGuides'
    static final String GUIDES_YML_PATH = 'conf/guides.yml'
    static final String VENDOR_TEMPLATE_PATH = 'buildSrc/src/main/template'

    static void apply(Project project) {
        File guidesYml = project.rootProject.layout.projectDirectory
                .file(GUIDES_YML_PATH).asFile

        if (!guidesYml.isFile()) {
            return
        }

        Directory templateRoot = project.rootProject.layout.projectDirectory
                .dir(VENDOR_TEMPLATE_PATH)

        List<String> registeredTaskNames = registerPerVersionTasks(
                project, templateRoot, guidesYml)

        registerAggregateTask(project, registeredTaskNames)
    }

    @CompileDynamic
    private static List<String> registerPerVersionTasks(
            Project project, Directory templateRoot, File guidesYml) {

        Map root = guidesYml.withReader('UTF-8') { reader ->
            new Yaml().load(reader) as Map
        }
        List guides = (root.guides ?: []) as List

        List<String> registeredTaskNames = []

        for (Map guide : guides) {
            String guideName = guide.name as String
            if (!guideName) continue

            Map versions = (guide.versions ?: [:]) as Map
            for (Map.Entry versionEntry : versions.entrySet()) {
                String versionKey = versionEntry.key as String
                if (!(versionEntry.value instanceof Map)) continue
                Map version = versionEntry.value as Map

                String sourcePath = version.sourcePath as String
                if (!sourcePath) continue

                File versionDir = project.rootProject.layout.projectDirectory
                        .file(sourcePath).asFile
                File adocDir = new File(versionDir, 'guide')
                if (!adocDir.isDirectory()) continue   // skip-if-missing

                File manifestFile = new File(versionDir, 'manifest.yml')
                Map<String, Object> attributes = manifestToAttributes(
                        manifestFile, guide, version, versionKey)

                String taskName = renderTaskName(guideName, versionKey)
                project.tasks.register(taskName, PublishGuideTask) { task ->
                    task.group = GROUP
                    task.description =
                            "Renders ${guideName} v${versionKey} via the vendored grails-doc renderer"
                    task.sourceDir.set(adocDir)
                    task.resourcesDir.set(templateRoot)
                    task.targetDir.set(
                            project.layout.buildDirectory.dir(
                                    "dist/guides/${guideName}/${versionKey}"))
                    task.asciidoc.set(true)
                    task.properties.set(attributes)
                }
                registeredTaskNames << taskName
            }
        }

        registeredTaskNames
    }

    private static void registerAggregateTask(
            Project project, List<String> taskNames) {
        project.tasks.register(AGGREGATE_TASK) { task ->
            task.group = GROUP
            task.description =
                    'Renders every wired-up guide-version pair under build/dist/guides/'
            for (String name : taskNames) {
                task.dependsOn(name)
            }
        }
    }

    /**
     * Produces a Gradle-task-safe identifier for a {@code (guide, version)} pair.
     * Replaces any non-alphanumeric character with an underscore so guide
     * names containing hyphens or dots remain valid Gradle task names.
     */
    static String renderTaskName(String guideName, String versionKey) {
        String safeName = guideName.replaceAll(/[^A-Za-z0-9]/, '_')
        String safeVersion = versionKey.replaceAll(/[^A-Za-z0-9]/, '_')
        "renderGuide_${safeName}_${safeVersion}"
    }

    /**
     * Builds the {@code attributes} map for {@link PublishGuideTask#getProperties()}.
     *
     * <p>Precedence (high to low):</p>
     * <ol>
     *   <li>Fields in the per-version {@code manifest.yml}</li>
     *   <li>Fields in the parent {@code conf/guides.yml} guide entry</li>
     *   <li>Synthetic {@code version} / {@code grails.version} from the version key</li>
     * </ol>
     *
     * <p>List-typed manifest fields (e.g. {@code authors}, {@code tags}) are
     * joined with {@code ", "} into a string because AsciiDoc attribute values
     * must be scalar.</p>
     */
    @CompileDynamic
    private static Map<String, Object> manifestToAttributes(
            File manifestFile, Map guide, Map version, String versionKey) {
        Map<String, Object> attrs = [:]

        if (manifestFile.isFile()) {
            Map manifest = manifestFile.withReader('UTF-8') { reader ->
                new Yaml().load(reader) as Map
            }
            manifest.each { Object key, Object value ->
                if (key instanceof String && value != null) {
                    attrs[key as String] = value instanceof List
                            ? (value as List).join(', ')
                            : value.toString()
                }
            }
        }

        // Fallbacks from the conf/guides.yml entry
        if (guide.title) attrs.putIfAbsent('title', guide.title.toString())
        if (guide.subtitle) attrs.putIfAbsent('subtitle', guide.subtitle.toString())
        if (guide.category) attrs.putIfAbsent('category', guide.category.toString())
        if (guide.authors instanceof List) {
            attrs.putIfAbsent('author', (guide.authors as List).join(', '))
        }
        if (version.publicationDate) {
            attrs.putIfAbsent('publicationDate', version.publicationDate.toString())
        } else if (guide.publicationDate) {
            attrs.putIfAbsent('publicationDate', guide.publicationDate.toString())
        }
        if (version.tags instanceof List) {
            attrs.putIfAbsent('tags', (version.tags as List).join(', '))
        }

        // Always surface the version key so :version{} attribute resolutions work.
        attrs.putIfAbsent('version', versionKey)
        attrs.putIfAbsent('grails.version', versionKey)

        attrs
    }
}

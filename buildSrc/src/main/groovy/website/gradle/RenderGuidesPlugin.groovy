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
import website.gradle.tasks.AssetsTask
import website.gradle.tasks.BlogTask
import website.gradle.tasks.BskyAtProtoDidTask
import website.gradle.tasks.DocumentationTask
import website.gradle.tasks.DownloadTask
import website.gradle.tasks.GuidesTask
import website.gradle.tasks.HtaccessTask
import website.gradle.tasks.MinutesTask
import website.gradle.tasks.ParityCheckGuideTask
import website.gradle.tasks.PluginsTask
import website.gradle.tasks.ProfilesTask
import website.gradle.tasks.QuestionsTask
import website.gradle.tasks.RenderSiteTask
import website.gradle.tasks.SitemapTask
import website.gradle.tasks.VendorGuideTask

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
    static final String MIGRATION_GROUP = 'migration'
    static final String AGGREGATE_TASK = 'buildAllGuides'
    static final String PARITY_AGGREGATE_TASK = 'parityCheckAllGuides'
    static final String VENDOR_AGGREGATE_TASK = 'vendorAllGuides'
    static final String GUIDES_YML_PATH = 'conf/guides.yml'
    static final String VENDOR_TEMPLATE_PATH = 'buildSrc/src/main/template'
    static final String PARITY_BASELINE_ROOT = 'buildSrc/src/test/resources/parity-baseline'
    /**
     * Default upstream-checkout root, relative to the project root's
     * sibling. Maps to {@code C:\Users\james\Documents\IdeaProjects\grails-guides-org\}
     * for the canonical workspace layout. Overridable via
     * {@code -PguidesWorkspaceRoot=/path/to/grails-guides-org}.
     */
    static final String DEFAULT_GUIDES_WORKSPACE_REL = '../grails-guides-org'

    static void apply(Project project) {
        File guidesYml = project.rootProject.layout.projectDirectory
                .file(GUIDES_YML_PATH).asFile

        if (!guidesYml.isFile()) {
            return
        }

        Directory templateRoot = project.rootProject.layout.projectDirectory
                .dir(VENDOR_TEMPLATE_PATH)

        Wiring wiring = registerPerVersionTasks(
                project, templateRoot, guidesYml)

        registerAggregateTask(project, GROUP, AGGREGATE_TASK,
                'Renders every wired-up guide-version pair under build/dist/guides/',
                wiring.renderTaskNames)
        registerAggregateTask(project, GROUP, PARITY_AGGREGATE_TASK,
                'Runs renderer parity checks for every guide-version that has a baseline snapshot under buildSrc/src/test/resources/parity-baseline/',
                wiring.parityTaskNames)
        registerAggregateTask(project, MIGRATION_GROUP, VENDOR_AGGREGATE_TASK,
                'Re-vendors every guide-version whose upstream sample repo is checked out under the workspace root (default ../grails-guides-org/, override with -PguidesWorkspaceRoot=...).',
                wiring.vendorTaskNames)
    }

    private static class Wiring {
        List<String> renderTaskNames = []
        List<String> parityTaskNames = []
        List<String> vendorTaskNames = []
    }

    @CompileDynamic
    private static Wiring registerPerVersionTasks(
            Project project, Directory templateRoot, File guidesYml) {

        Map root = guidesYml.withReader('UTF-8') { reader ->
            new Yaml().load(reader) as Map
        }
        List guides = (root.guides ?: []) as List

        Wiring wiring = new Wiring()

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

                String safeName = sanitize(guideName)
                String safeVersion = sanitize(versionKey)

                // Vendor task -- registered on its own (BEFORE the
                // skip-if-missing guard for fixture presence) so the
                // task can be invoked to CREATE the fixture for guides
                // not yet vendored on disk. Gated only by sampleRef
                // metadata + presence of an upstream checkout.
                Map sampleRefEarly = (version.sampleRef ?: [:]) as Map
                String repoSlugEarly = sampleRefEarly.repo as String
                String repoShaEarly = sampleRefEarly.sha as String
                String repoBranchEarly = sampleRefEarly.branch as String
                if (repoSlugEarly && repoShaEarly) {
                    String workspaceRoot = project.findProperty('guidesWorkspaceRoot') as String ?:
                            new File(project.rootProject.layout.projectDirectory.asFile, DEFAULT_GUIDES_WORKSPACE_REL).canonicalPath
                    String repoName = repoSlugEarly.tokenize('/').last()
                    File checkoutDir = new File(workspaceRoot, repoName)
                    if (checkoutDir.isDirectory()) {
                        String vendorTaskName = "vendorGuide_${safeName}_${safeVersion}"
                        File commonDir = project.rootProject.layout.projectDirectory.dir('guides/common').asFile
                        File destDir = new File(versionDir.parentFile, versionDir.name)
                        Map<String, Object> manifestData = [
                                name: guideName,
                                version: versionKey,
                                title: guide.title,
                                subtitle: guide.subtitle,
                                authors: guide.authors,
                                category: guide.category,
                                tags: version.tags ?: [],
                                publicationDate: version.publicationDate ?: guide.publicationDate,
                                githubSlug: repoSlugEarly,
                                githubBranch: repoBranchEarly,
                                githubSha: repoShaEarly,
                        ] as Map<String, Object>
                        project.tasks.register(vendorTaskName, VendorGuideTask) { VendorGuideTask task ->
                            task.group = MIGRATION_GROUP
                            task.description = "Re-vendor ${guideName} v${versionKey} from upstream ${repoSlugEarly}@${repoShaEarly[0..6]}. Overwrites ${versionDir.name}/."
                            task.sampleRepoRoot.set(checkoutDir)
                            task.commonDir.set(commonDir)
                            task.destDir.set(destDir)
                            task.manifest.set(manifestData)
                        }
                        wiring.vendorTaskNames << vendorTaskName
                    }
                }

                if (!adocDir.isDirectory()) continue   // skip-if-missing for render/parity/stage; vendor is registered above

                File manifestFile = new File(versionDir, 'manifest.yml')
                Map<String, Object> attributes = manifestToAttributes(
                        manifestFile, guide, version, versionKey)

                String stageTaskName = "stageGuideSource_${safeName}_${safeVersion}"
                String renderTaskName = "renderGuide_${safeName}_${safeVersion}"
                String stagedRelPath = "staged-guides/${guideName}/${versionKey}"

                // Stage the per-version source into a working layout that
                // DocPublisher expects: <staged>/guide/*.adoc + toc.yml.
                // Our fixture stores toc.yml at the version root (alongside
                // manifest.yml) for self-containedness; the renderer wants
                // it inside guide/. The Sync task does the rearrangement
                // without mutating the fixture in source control.
                project.tasks.register(stageTaskName, org.gradle.api.tasks.Sync) { stage ->
                    stage.group = GROUP
                    stage.description = "Stages ${guideName} v${versionKey} source for the vendored grails-doc renderer"
                    stage.into(project.layout.buildDirectory.dir(stagedRelPath))
                    stage.from(versionDir) {
                        // Top-level toc.yml is repositioned into guide/ below.
                        exclude 'toc.yml'
                    }
                    File rootToc = new File(versionDir, 'toc.yml')
                    if (rootToc.isFile()) {
                        stage.from(rootToc) {
                            into 'guide'
                        }
                    }
                }

                project.tasks.register(renderTaskName, PublishGuideTask) { task ->
                    task.group = GROUP
                    task.description =
                            "Renders ${guideName} v${versionKey} via the vendored grails-doc renderer"
                    task.dependsOn(stageTaskName)
                    task.sourceDir.set(
                            project.layout.buildDirectory.dir(stagedRelPath))
                    task.resourcesDir.set(templateRoot)
                    task.targetDir.set(
                            project.layout.buildDirectory.dir(
                                    "dist/guides/${guideName}/${versionKey}"))
                    task.asciidoc.set(true)
                    task.properties.set(attributes)
                    // The vendored PublishGuideTask holds a Project reference
                    // and an AntBuilder, neither of which serialize for
                    // Gradle's configuration cache. Annotating here (instead
                    // of editing the vendored source) keeps the upstream
                    // copy byte-clean for re-vendor.
                    task.notCompatibleWithConfigurationCache(
                            'Vendored grails.doc.gradle.PublishGuideTask references Project + AntBuilder')
                    // Several main-site rendering tasks declare build/ as their
                    // @OutputDirectory (they write to build/dist/<x>.html), which
                    // overlaps with build/staged-guides/<name>/<v>/. Without
                    // explicit ordering, Gradle 9.4 raises a fatal validation
                    // error: 'renderGuide_* uses output of :renderSite/:genPlugins/
                    // :renderBlog/:renderMinutes/:genProfilesPage without declaring
                    // dependency'. mustRunAfter resolves the ordering without
                    // forcing those tasks to be invoked when only renderGuide_* is
                    // scheduled. Names come from each task class's NAME constant
                    // so a future task rename surfaces as a compile error here.
                    task.mustRunAfter(RenderSiteTask.NAME, PluginsTask.NAME,
                            BlogTask.NAME, MinutesTask.NAME, ProfilesTask.NAME,
                            HtaccessTask.NAME, BskyAtProtoDidTask.NAME,
                            SitemapTask.NAME, AssetsTask.NAME,
                            DocumentationTask.NAME, DownloadTask.NAME,
                            QuestionsTask.NAME, GuidesTask.NAME)
                }
                wiring.renderTaskNames << renderTaskName

                // Parity check vs the legacy snapshot, when one exists on disk.
                File baselineFile = project.rootProject.layout.projectDirectory
                        .file("${PARITY_BASELINE_ROOT}/${guideName}-v${versionKey}/index.html").asFile
                if (baselineFile.isFile()) {
                    String parityTaskName = "parityCheckGuide_${safeName}_${safeVersion}"
                    String renderedSinglePage = "dist/guides/${guideName}/${versionKey}/guide/single.html"
                    String reportRelPath = "reports/parity/${guideName}/${versionKey}.md"
                    project.tasks.register(parityTaskName, ParityCheckGuideTask) { ParityCheckGuideTask task ->
                        task.group = GROUP
                        task.description = "Compares rendered ${guideName} v${versionKey} against the legacy snapshot at ${baselineFile.name}"
                        task.dependsOn(renderTaskName)
                        task.localFile.set(project.layout.buildDirectory.file(renderedSinglePage))
                        task.baselineFile.set(baselineFile)
                        task.reportFile.set(project.layout.buildDirectory.file(reportRelPath))
                        task.guideLabel.set("${guideName}@v${versionKey}")
                        if (project.hasProperty('parityFailOnDiff')) {
                            task.failOnDiff.set(Boolean.parseBoolean(project.property('parityFailOnDiff') as String))
                        }
                    }
                    wiring.parityTaskNames << parityTaskName
                }
            }
        }

        wiring
    }

    private static String sanitize(String value) {
        value.replaceAll(/[^A-Za-z0-9]/, '_')
    }

    private static void registerAggregateTask(
            Project project, String taskGroup, String aggregateName, String description, List<String> taskNames) {
        project.tasks.register(aggregateName) { task ->
            task.group = taskGroup
            task.description = description
            if (aggregateName == AGGREGATE_TASK) {
                task.notCompatibleWithConfigurationCache('Runs vendored PublishGuide tasks and their log-capture finalizers.')
            }
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
        "renderGuide_${sanitize(guideName)}_${sanitize(versionKey)}"
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

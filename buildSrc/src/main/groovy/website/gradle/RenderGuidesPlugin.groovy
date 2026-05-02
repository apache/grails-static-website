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
import java.util.regex.Matcher
import java.util.regex.Pattern
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
 * {@code guides/resources/} and are passed to
 * {@link PublishGuideTask#getResourcesDir()} as a filesystem path. The
 * {@code guides/resources/style/layout.html} template renders the legacy
 * single-page guide chrome; {@code guideItem.html}, {@code section.html},
 * {@code index.html}, {@code menu.html}, and {@code referenceItem.html}
 * round out the bundle DocPublisher expects.</p>
 */
@CompileStatic
class RenderGuidesPlugin {

    static final String GROUP = 'documentation'
    static final String AGGREGATE_TASK = 'buildAllGuides'
    static final String PARITY_AGGREGATE_TASK = 'parityCheckAllGuides'
    static final String GUIDES_YML_PATH = 'conf/guides.yml'
    static final String GUIDE_TEMPLATE_PATH = 'guides/resources'
    static final String PARITY_BASELINE_ROOT = 'buildSrc/src/test/resources/parity-baseline'

    static void apply(Project project) {
        File guidesYml = project.rootProject.layout.projectDirectory
                .file(GUIDES_YML_PATH).asFile

        if (!guidesYml.isFile()) {
            return
        }

        Directory templateRoot = project.rootProject.layout.projectDirectory
                .dir(GUIDE_TEMPLATE_PATH)

        Wiring wiring = registerPerVersionTasks(
                project, templateRoot, guidesYml)

        registerAggregateTask(project, GROUP, AGGREGATE_TASK,
                'Renders every wired-up guide-version pair under build/dist/guides/',
                wiring.renderTaskNames)
        registerAggregateTask(project, GROUP, PARITY_AGGREGATE_TASK,
                'Runs renderer parity checks for every guide-version that has a baseline snapshot under buildSrc/src/test/resources/parity-baseline/',
                wiring.parityTaskNames)
    }

    private static class Wiring {
        List<String> renderTaskNames = []
        List<String> parityTaskNames = []
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

                if (!adocDir.isDirectory()) continue   // skip-if-missing for render/parity/stage

                File manifestFile = new File(versionDir, 'manifest.yml')
                Map<String, Object> attributes = manifestToAttributes(
                        manifestFile, guide, version, versionKey)
                injectSitePartials(project, attributes)

                String stageTaskName = "stageGuideSource_${safeName}_${safeVersion}"
                String renderTaskName = "renderGuide_${safeName}_${safeVersion}"
                String stagedRelPath = "staged-guides/${guideName}/${versionKey}"

                // Stage the per-version source into a working layout that
                // DocPublisher expects: <staged>/guide/*.adoc + toc.yml.
                File commonDirSrc = project.rootProject.layout.projectDirectory
                        .dir('guides/common').asFile
                project.tasks.register(stageTaskName, org.gradle.api.tasks.Sync) { stage ->
                    stage.group = GROUP
                    stage.description = "Stages ${guideName} v${versionKey} source for the vendored grails-doc renderer"
                    stage.into(project.layout.buildDirectory.dir(stagedRelPath))
                    stage.from(versionDir) {
                        exclude 'toc.yml'
                    }
                    File rootToc = new File(versionDir, 'toc.yml')
                    if (rootToc.isFile()) {
                        stage.from(rootToc) {
                            into 'guide'
                        }
                    }
                    // The vendored grails-doc renderer drives AsciidoctorJ
                    // without a baseDir, so include::{commondir}/foo.adoc[]
                    // and include::../snippets/...[] never resolve. Inline
                    // both kinds of include into each staged .adoc at
                    // config time instead. img/ is staged as part of the
                    // top-level Sync.from(versionDir) above; the per-task
                    // doLast (see renderTaskName) copies it into dist.
                    File snippetsDirSrc = new File(versionDir, 'snippets')
                    stage.doLast {
                        File guideDir = project.layout.buildDirectory.dir(stagedRelPath).get().asFile
                        File guideAdocDir = new File(guideDir, 'guide')
                        RenderGuidesPlugin.inlineCommonIncludes(guideAdocDir, commonDirSrc)
                        RenderGuidesPlugin.inlineSnippetIncludes(guideAdocDir, snippetsDirSrc)
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
                    // Drives the "Improve this doc" buttons in section.html.
                    task.sourceRepo.set("https://github.com/apache/grails-static-website/edit/master/${sourcePath}".toString())
                    task.notCompatibleWithConfigurationCache(
                            'Vendored grails.doc.gradle.PublishGuideTask references Project + AntBuilder')
                    // Order after the main-site tasks since they share build/ as @OutputDirectory.
                    task.mustRunAfter(RenderSiteTask.NAME, PluginsTask.NAME,
                            BlogTask.NAME, MinutesTask.NAME, ProfilesTask.NAME,
                            HtaccessTask.NAME, BskyAtProtoDidTask.NAME,
                            SitemapTask.NAME, AssetsTask.NAME,
                            DocumentationTask.NAME, DownloadTask.NAME,
                            QuestionsTask.NAME, GuidesTask.NAME)
                    // The DocPublisher emits the legacy single-page chrome to
                    // single.html and a TOC-only stub to index.html. Promote the
                    // single page to the canonical /guide/index.html URL.
                    // Also propagate img/ from staged source into the
                    // dist tree so `<img src="../img/foo.png">` resolves.
                    task.doLast {
                        File targetRoot = task.targetDir.get().asFile
                        File singleHtml = new File(targetRoot, 'guide/single.html')
                        if (singleHtml.isFile()) {
                            File indexHtml = new File(targetRoot, 'guide/index.html')
                            indexHtml.bytes = singleHtml.bytes
                        }
                        File stagedImgDir = project.layout.buildDirectory
                                .dir(stagedRelPath + '/img').get().asFile
                        if (stagedImgDir.isDirectory()) {
                            File destImgDir = new File(targetRoot, 'img')
                            destImgDir.mkdirs()
                            stagedImgDir.eachFileRecurse { File src ->
                                if (!src.isFile()) return
                                String relPath = stagedImgDir.toPath()
                                        .relativize(src.toPath()).toString()
                                File dest = new File(destImgDir, relPath)
                                dest.parentFile.mkdirs()
                                dest.bytes = src.bytes
                            }
                        }
                    }
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
    /**
     * Reads the shared chrome partials from {@code templates/partials/} and
     * exposes them as {@code siteHead}, {@code siteHeader}, {@code siteFooter}
     * so the legacy guide layout can substitute them via Groovy {@code ${...}}.
     */
    private static void injectSitePartials(Project project, Map<String, Object> attrs) {
        ['siteHead': 'site-head', 'siteHeader': 'site-header', 'siteFooter': 'site-footer'].each { key, partial ->
            File f = project.rootProject.layout.projectDirectory
                    .file("templates/partials/${partial}.html").asFile
            if (f.isFile()) {
                attrs.put(key, f.getText('UTF-8'))
            }
        }
    }

    /**
     * Replaces {@code include::{commondir}/common-*.adoc[]} directives in
     * every staged .adoc with the literal contents of the common snippet.
     * The vendored renderer's AsciidoctorJ wrapper has no baseDir, so
     * include resolution would otherwise fall through.
     */
    @CompileDynamic
    static void inlineCommonIncludes(File guideAdocDir, File commonDir) {
        if (!guideAdocDir.isDirectory() || !commonDir.isDirectory()) {
            return
        }
        Pattern pat = Pattern.compile(/include::\{commondir\}\/(common-[\w\-]+\.adoc)\[\]/)
        guideAdocDir.eachFileRecurse { File f ->
            if (!f.isFile() || !f.name.endsWith('.adoc')) return
            String text = f.getText('UTF-8')
            Matcher m = pat.matcher(text)
            if (!m.find()) return
            StringBuilder out = new StringBuilder()
            int last = 0
            m.reset()
            while (m.find()) {
                out.append(text, last, m.start())
                File snippet = new File(commonDir, m.group(1))
                if (snippet.isFile()) {
                    out.append(snippet.getText('UTF-8'))
                } else {
                    out.append("// missing include: ${m.group(1)}\n".toString())
                }
                last = m.end()
            }
            out.append(text, last, text.length())
            f.setText(out.toString(), 'UTF-8')
        }
    }

    /**
     * Replaces {@code include::../snippets/<path>[<attrs>]} directives in
     * every staged .adoc with the literal contents of the snippet file under
     * {@code snippetsDir}, with AsciiDoc-equivalent attribute filtering
     * applied. The vendored renderer's AsciidoctorJ wrapper has no baseDir
     * and runs in the default {@code SafeMode.SECURE}, so include resolution
     * would otherwise fall through and AsciidoctorJ would emit literal
     * {@code link:...[role=include]} text into the published HTML.
     *
     * <p>Supports the four attribute styles that account for 100% of usage
     * across the vendored guides ({@code []}, {@code [indent=N]},
     * {@code [tag(s)=foo,bar]}, {@code [lines=N..M[,P..Q]]}).</p>
     *
     * <p>Three include shapes are recognised:</p>
     * <ul>
     *   <li>{@code include::../snippets/<path>[...]} - chapter directly
     *       under {@code guide/}.</li>
     *   <li>{@code include::../../snippets/<path>[...]} (or deeper) -
     *       chapter living in a subdirectory below {@code guide/}, e.g.
     *       {@code guide/writingTheApp/configurationProperties.adoc} which
     *       needs to escape both {@code writingTheApp/} and {@code guide/}.
     *       Verified against 47 affected files across 6 guides; 4 files
     *       use {@code ../../../} (three levels).</li>
     *   <li>{@code include::{sourcedir}/../<path>[...]} - legacy upstream
     *       grails-doc form. The renderer's AsciidoctorJ wrapper does not
     *       define {@code sourcedir}, so this pattern would otherwise fall
     *       through. The path is treated as snippets-relative because the
     *       Phase 9 vendoring stripped the {@code complete/} / {@code initial/}
     *       prefix when copying upstream files into {@code snippets/}.
     *       21 occurrences in the android guide.</li>
     * </ul>
     */
    @CompileDynamic
    static void inlineSnippetIncludes(File guideAdocDir, File snippetsDir) {
        if (!guideAdocDir.isDirectory()) {
            return
        }
        // Two patterns, same handler. The relative-path form supports any
        // positive number of leading `../` segments. The {sourcedir} form is
        // a legacy upstream-grails-doc shape kept around so vendored guide
        // sources don't have to be rewritten.
        List<Pattern> patterns = [
                Pattern.compile(/include::(?:\.\.\/)+snippets\/([^\[\s]+)\[([^\]]*)\]/),
                Pattern.compile(/include::\{sourcedir\}\/\.\.\/([^\[\s]+)\[([^\]]*)\]/),
        ]
        guideAdocDir.eachFileRecurse { File f ->
            if (!f.isFile() || !f.name.endsWith('.adoc')) return
            String text = f.getText('UTF-8')
            String currentText = text
            boolean changed = false
            for (Pattern pat : patterns) {
                Matcher m = pat.matcher(currentText)
                if (!m.find()) continue
                changed = true
                StringBuilder out = new StringBuilder()
                int last = 0
                m.reset()
                while (m.find()) {
                    out.append(currentText, last, m.start())
                    String snippetRel = m.group(1)
                    String attrStr = m.group(2)
                    File snippet = snippetsDir != null ? new File(snippetsDir, snippetRel) : null
                    if (snippet != null && snippet.isFile()) {
                        out.append(applySnippetAttributes(snippet.getText('UTF-8'), attrStr))
                    } else {
                        out.append("// missing snippet: ${snippetRel}\n".toString())
                    }
                    last = m.end()
                }
                out.append(currentText, last, currentText.length())
                currentText = out.toString()
            }
            if (changed) {
                f.setText(currentText, 'UTF-8')
            }
        }
    }

    /**
     * Applies AsciiDoc include attribute filters ({@code tag}/{@code tags},
     * {@code lines}, {@code indent}) to a snippet body. Order: tag/lines
     * filtering first (selects which lines are kept), then indent
     * normalisation on the survivors.
     */
    @CompileDynamic
    static String applySnippetAttributes(String body, String attrStr) {
        Map<String, String> attrs = parseIncludeAttributes(attrStr)
        String tagAttr = attrs.get('tag') ?: attrs.get('tags')
        String linesAttr = attrs.get('lines')
        String indentAttr = attrs.get('indent')

        String result = body
        if (tagAttr) {
            result = extractTaggedRegions(result, tagAttr)
        } else if (linesAttr) {
            result = extractLineRanges(result, linesAttr)
        }
        if (indentAttr != null) {
            result = applyIndent(result, indentAttr)
        }
        // AsciiDoc include directives never include the trailing newline of
        // the host line; the surrounding ---- delimiters supply their own
        // line breaks. Trim a single trailing \n so re-emitted snippets
        // don't add a stray blank line between the body and the closing
        // ---- in source listing blocks.
        if (result.endsWith('\n')) {
            result = result.substring(0, result.length() - 1)
        }
        result
    }

    private static Map<String, String> parseIncludeAttributes(String attrStr) {
        Map<String, String> attrs = [:]
        if (!attrStr) return attrs
        // Naive comma split is wrong because tags=a,b is a single attr with
        // a comma-bearing value. Split on commas that precede a `name=`
        // token instead. Each piece then splits on the first `=`.
        List<String> parts = []
        int start = 0
        Pattern attrStart = Pattern.compile(/,(?=\s*[A-Za-z_][\w\-]*\s*=)/)
        Matcher m = attrStart.matcher(attrStr)
        while (m.find()) {
            parts << attrStr.substring(start, m.start())
            start = m.end()
        }
        parts << attrStr.substring(start)
        parts.each { String piece ->
            int eq = piece.indexOf('=')
            if (eq < 0) {
                String key = piece.trim()
                if (key) attrs.put(key, '')
            } else {
                String key = piece.substring(0, eq).trim()
                String val = piece.substring(eq + 1).trim()
                if (val.startsWith('"') && val.endsWith('"') && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1)
                }
                attrs.put(key, val)
            }
        }
        attrs
    }

    /**
     * Extracts AsciiDoc-tagged regions ({@code tag::name[]} ...
     * {@code end::name[]}). Honours the standard wildcards: {@code *}
     * (everything except marker lines), {@code **} (regions inside any
     * tagged block), and a leading {@code !} to exclude a tag.
     */
    @CompileDynamic
    static String extractTaggedRegions(String body, String tagSpec) {
        List<String> tokens = tagSpec.split(',').collect { it.trim() }.findAll { it }
        Set<String> includeTags = tokens.findAll { !it.startsWith('!') } as Set
        Set<String> excludeTags = tokens.findAll { it.startsWith('!') }.collect { it.substring(1) } as Set
        boolean wildcardAll = includeTags.contains('*') || includeTags.contains('**')
        Pattern markerPat = Pattern.compile(/(?:^|\W)(tag|end)::([\w\-]+)\[\]/)

        List<String> outLines = []
        List<String> activeTags = []
        body.split(/\r?\n/, -1).each { String line ->
            Matcher mm = markerPat.matcher(line)
            if (mm.find()) {
                String kind = mm.group(1)
                String name = mm.group(2)
                if (kind == 'tag') {
                    activeTags << name
                } else if (kind == 'end') {
                    int idx = activeTags.lastIndexOf(name)
                    if (idx >= 0) activeTags.remove(idx)
                }
                return  // marker lines themselves are never emitted
            }
            boolean inExcluded = activeTags.any { excludeTags.contains(it) }
            if (inExcluded) return
            boolean inIncluded = wildcardAll
                    ? !activeTags.isEmpty() || includeTags.contains('*')
                    : activeTags.any { includeTags.contains(it) }
            if (inIncluded) {
                outLines << line
            }
        }
        outLines.join('\n')
    }

    /**
     * Extracts AsciiDoc {@code lines=N..M[,P..Q]} ranges from a snippet body.
     * Single-line specs ({@code lines=N}) and open-ended ranges
     * ({@code lines=N..-1}) are honoured.
     */
    @CompileDynamic
    static String extractLineRanges(String body, String linesSpec) {
        String[] all = body.split(/\r?\n/, -1)
        Set<Integer> keep = new TreeSet<>()
        linesSpec.split(/[,;]/).each { String range ->
            range = range.trim()
            if (!range) return
            int dotdot = range.indexOf('..')
            if (dotdot < 0) {
                int n = range.toInteger()
                if (n >= 1 && n <= all.length) keep.add(n - 1)
            } else {
                int start = range.substring(0, dotdot).toInteger()
                String endStr = range.substring(dotdot + 2)
                int end = endStr ? endStr.toInteger() : all.length
                if (end < 0) end = all.length
                start = Math.max(1, start)
                end = Math.min(all.length, end)
                for (int i = start; i <= end; i++) keep.add(i - 1)
            }
        }
        StringBuilder sb = new StringBuilder()
        boolean first = true
        keep.each { Integer i ->
            if (!first) sb.append('\n')
            sb.append(all[i])
            first = false
        }
        sb.toString()
    }

    /**
     * Applies the AsciiDoc {@code indent} attribute. {@code indent=0} strips
     * the smallest common leading whitespace from every non-blank line.
     * {@code indent=N} (N>0) strips that common indent and replaces it with
     * N spaces.
     */
    @CompileDynamic
    static String applyIndent(String body, String indentAttr) {
        int target
        try {
            target = indentAttr.toInteger()
        } catch (NumberFormatException ignored) {
            return body
        }
        if (target < 0) return body
        String[] lines = body.split(/\r?\n/, -1)
        int common = Integer.MAX_VALUE
        for (String line : lines) {
            if (!line.trim()) continue
            int n = 0
            while (n < line.length() && line.charAt(n) == ' ') n++
            if (n < common) common = n
            if (common == 0) break
        }
        if (common == Integer.MAX_VALUE) common = 0
        String pad = ' ' * target
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i]
            String stripped = line.length() >= common ? line.substring(common) : line.trim()
            if (line.trim()) {
                sb.append(pad).append(stripped)
            } else {
                sb.append(stripped)
            }
            if (i < lines.length - 1) sb.append('\n')
        }
        sb.toString()
    }

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
        // Legacy guide layout templates expect these names directly.
        attrs.putIfAbsent('grailsVersion', versionKey)
        if (guide.authors instanceof List) {
            attrs.putIfAbsent('authors', (guide.authors as List).join(', '))
        }
        attrs
    }
}

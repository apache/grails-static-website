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

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import website.asciidoc.SnippetVendoringIncludeProcessor

/**
 * Bulk-migration tool: takes a pre-cloned upstream guide repo at a
 * pinned SHA, walks every {@code .adoc} under {@code src/main/docs/guide/},
 * runs them through AsciiDoctor with the
 * {@link SnippetVendoringIncludeProcessor} extension, and emits
 *
 * <pre>
 * guides/&lt;name&gt;/v&lt;version&gt;/
 * |- toc.yml                       (copied verbatim from upstream)
 * |- manifest.yml                  (per-version metadata; written here)
 * |- guide/
 * |  |- *.adoc                     (rewritten so include::{sourceDir}/...
 * |                                 -> include::../snippets/...)
 * `- snippets/
 *    |- MANIFEST.yml               (audit record of every vendored snippet)
 *    `- ...                        (snippet files copied verbatim from upstream,
 *                                   directory layout preserved minus the leading
 *                                   complete/ or initial/ segment)
 * </pre>
 *
 * <p>Designed to be invoked once per guide-version during Phase 10
 * (bulk migration of the remaining ~124 guides). Each invocation is a
 * one-shot operation: the resulting tree is committed to git as the
 * fixture, and the original upstream checkout is no longer needed for
 * day-to-day building.</p>
 */
@CompileStatic
abstract class VendorGuideTask extends DefaultTask {

    static final String GROUP = 'migration'

    /** Upstream sample repo's local working tree, checked out at a pinned SHA. */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getSampleRepoRoot()

    /** Common-snippets directory ({@code guides/common/}). */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getCommonDir()

    /** Where the vendored fixture tree should be written. */
    @OutputDirectory
    abstract DirectoryProperty getDestDir()

    /** Per-version metadata serialised into {@code destDir/manifest.yml}. */
    @Input
    abstract MapProperty<String, Object> getManifest()

    @TaskAction
    void vendor() {
        File sampleRoot = sampleRepoRoot.get().asFile
        File common = commonDir.get().asFile
        File dest = destDir.get().asFile
        Map<String, Object> manifestData = manifest.get()

        File guideSrcDir = new File(sampleRoot, 'src/main/docs/guide')
        if (!guideSrcDir.isDirectory()) {
            throw new GradleException(
                    "Sample repo at ${sampleRoot} has no src/main/docs/guide/ -- " +
                            'is the SHA correct? Has the upstream layout changed?')
        }

        File destGuideDir = new File(dest, 'guide')
        File destSnippetsDir = new File(dest, 'snippets')
        // Clean before writing so files removed (or moved) upstream don't
        // linger in the fixture across re-vendor runs. We deliberately
        // recreate only the two managed subtrees rather than wiping
        // {@code dest} entirely so any sibling files outside this task's
        // contract (e.g. a hand-curated note file) survive.
        if (destGuideDir.exists()) destGuideDir.deleteDir()
        if (destSnippetsDir.exists()) destSnippetsDir.deleteDir()
        destGuideDir.mkdirs()
        destSnippetsDir.mkdirs()

        // Copy toc.yml verbatim from upstream guide/ into our v<n> root
        // (matches the manual-fixture convention from PR #446 + Phase 9a).
        File upstreamToc = new File(guideSrcDir, 'toc.yml')
        if (upstreamToc.isFile()) {
            new File(dest, 'toc.yml').bytes = upstreamToc.bytes
        }

        // Run AsciiDoctor with our extension. The extension copies snippet
        // files into destSnippetsDir AND rewrites include paths in the
        // adoc files. Since we want the rewrites in our DESTINATION
        // (guide/*.adoc), we run AsciiDoctor in convert mode but discard
        // the HTML; we keep only the side effects (snippet copies +
        // manifest accumulation).
        SnippetVendoringIncludeProcessor processor =
                new SnippetVendoringIncludeProcessor(sampleRoot, destSnippetsDir, common)

        // Walk recursively. Some upstream guides nest adocs under
        // subdirectories (e.g. writingTheApp/setupMultitenancy/foo.adoc)
        // and reference them from toc.yml as dotted keys. We must process
        // and copy every .adoc, not just the ones at guide/ root.
        List<File> upstreamAdocs = collectAdocs(guideSrcDir)

        Asciidoctor asciidoctor = Asciidoctor.Factory.create()
        try {
            asciidoctor.javaExtensionRegistry().includeProcessor(processor)
            Attributes attributes = Attributes.builder()
                    .attribute('sourceDir', new File(sampleRoot, 'complete').absolutePath)
                    .attribute('commondir', common.absolutePath)
                    .build()
            for (File adoc : upstreamAdocs) {
                Options options = Options.builder()
                        .safe(SafeMode.UNSAFE)
                        .standalone(false)
                        .toFile(false)
                        .baseDir(adoc.parentFile)
                        .attributes(attributes)
                        .build()
                asciidoctor.convert(adoc.text, options)
            }
        } finally {
            asciidoctor.shutdown()
        }

        // Mirror the upstream guide/ tree into our destination, applying
        // the include-path rewrite to every .adoc. Directory structure is
        // preserved so toc.yml's dotted keys resolve to nested files in
        // the staged tree at render time.
        for (File adoc : upstreamAdocs) {
            String rel = guideSrcDir.toPath().relativize(adoc.toPath()).toString().replace('\\', '/')
            File destAdoc = new File(destGuideDir, rel)
            if (!destAdoc.parentFile.exists()) {
                destAdoc.parentFile.mkdirs()
            }
            String rewritten = adoc.text.replaceAll(/include::\{sourceDir\}\//, 'include::../snippets/')
            // For nested adocs the relative '../snippets/' breaks: a file
            // at guide/writingTheApp/setupMultitenancy/foo.adoc needs to
            // walk up further. Compute the depth-aware prefix.
            int depth = rel.length() - rel.replace('/', '').length()
            if (depth > 0) {
                String upPrefix = ('../' * (depth + 1))
                rewritten = rewritten.replaceAll(/include::\.\.\/snippets\//, "include::${upPrefix}snippets/".toString())
            }
            destAdoc.text = rewritten
        }

        // Augment manifest data with the upstream commit timestamp,
        // queried from the local checkout. Doing it here (rather than
        // requiring callers to pass it in) keeps the task self-contained.
        Map<String, Object> augmented = new LinkedHashMap<>(manifestData)
        if (!augmented.containsKey('sourceCommitDate')) {
            String sha = augmented['githubSha'] as String
            if (sha) {
                String date = readUpstreamCommitDate(sampleRoot, sha)
                if (date) augmented['sourceCommitDate'] = date
            }
        }

        writeManifestYml(new File(dest, 'manifest.yml'), augmented)
        writeSnippetsManifestYml(new File(destSnippetsDir, 'MANIFEST.yml'), augmented, processor.manifest)

        logger.lifecycle('Vendored {} -> {} ({} snippet(s))',
                sampleRoot.name, dest.name, processor.manifest.size())
    }

    @CompileDynamic
    private static List<File> collectAdocs(File root) {
        List<File> out = []
        Deque<File> stack = new ArrayDeque<>()
        stack.push(root)
        while (!stack.empty) {
            File dir = stack.pop()
            File[] entries = dir.listFiles()
            if (entries == null) continue
            for (File f : entries) {
                if (f.isDirectory()) {
                    stack.push(f)
                } else if (f.name.endsWith('.adoc')) {
                    out << f
                }
            }
        }
        out
    }

    private static String readUpstreamCommitDate(File repoRoot, String sha) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    'git', '-C', repoRoot.absolutePath,
                    'log', '-1', '--format=%aI', sha)
            pb.redirectErrorStream(true)
            Process p = pb.start()
            String out = p.inputStream.text.trim()
            int rc = p.waitFor()
            return (rc == 0 && out) ? out : null
        } catch (Exception ignored) {
            return null
        }
    }

    @CompileDynamic
    private static void writeManifestYml(File target, Map<String, Object> data) {
        DumperOptions opts = new DumperOptions()
        opts.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        opts.prettyFlow = true
        target.text = '# Generated by :vendorGuide\n' + new Yaml(opts).dump(data)
    }

    @CompileDynamic
    private static void writeSnippetsManifestYml(File target, Map<String, Object> guideData,
            List<SnippetVendoringIncludeProcessor.ManifestEntry> entries) {
        Map<String, Object> top = [:]
        Map<String, Object> source = [:]
        source['repo'] = guideData['githubSlug']
        source['branch'] = guideData['githubBranch']
        source['sha'] = guideData['githubSha']
        // capturedAt is the vendoring timestamp (when this manifest was
        // written), not the upstream commit date. The upstream commit date
        // is recorded in manifest.yml's sourceCommitDate field.
        source['capturedAt'] = java.time.LocalDate.now().toString()
        top['source'] = source
        top['entries'] = entries.collect { entry ->
            [
                    vendoredPath: entry.vendoredPath,
                    upstreamPath: entry.upstreamPath,
            ] as Map<String, Object>
        }
        DumperOptions opts = new DumperOptions()
        opts.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        opts.prettyFlow = true
        target.text = '# Generated by :vendorGuide\n' + new Yaml(opts).dump(top)
    }
}

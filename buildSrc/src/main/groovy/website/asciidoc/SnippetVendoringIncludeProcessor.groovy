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
package website.asciidoc

import groovy.transform.CompileStatic
import groovy.transform.ToString

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.IncludeProcessor
import org.asciidoctor.extension.PreprocessorReader

/**
 * AsciiDoctor preprocessor extension that intercepts every
 * {@code include::...[]} directive at preprocess time and either
 * (a) vendors the referenced source file into a destination
 * snippets directory and rewrites the include path to point at the
 * vendored copy, or (b) passes the include through unchanged when
 * the target lives outside the sample repo (e.g. {@code commondir}
 * macros that resolve into {@code guides/common/}).
 *
 * <p>This extension powers Phase 10 bulk-migration automation: the
 * Gradle task that drives it can take a freshly cloned upstream guide
 * repo, point AsciiDoctor at the root {@code .adoc}, and have all the
 * {@code include::{sourceDir}/...} references mechanically rewritten
 * to {@code include::../snippets/...} while the snippet files
 * themselves get copied verbatim. {@code lines=}/{@code tag=}/{@code tags=}
 * filtering attributes are preserved -- AsciiDoctor's built-in
 * IncludeProcessor honours them at render time.</p>
 *
 * <p>Manifest entries (one per vendored snippet) are accumulated on
 * {@link #getManifest()} so the calling task can serialize a
 * {@code snippets/MANIFEST.yml} audit record matching the manual
 * fixtures.</p>
 */
@CompileStatic
class SnippetVendoringIncludeProcessor extends IncludeProcessor {

    /**
     * Audit record for one vendored include. The Gradle task that
     * orchestrates vendoring serializes a list of these into
     * {@code snippets/MANIFEST.yml}.
     */
    @ToString(includeNames = true)
    static class ManifestEntry {
        String vendoredPath          // relative to snippets/, e.g. 'grails-app/domain/Foo.groovy'
        String upstreamPath          // relative to sample repo root, e.g. 'complete/grails-app/domain/Foo.groovy'
        String filterLines           // value of `lines=` attribute on the include, if present
        String filterTag             // value of `tag=` or `tags=` attribute, if present
    }

    private final File sampleRepoRoot
    private final File snippetsOutDir
    private final File commonDir
    private final List<ManifestEntry> manifestEntries = []
    private final Set<String> vendoredAlready = [] as LinkedHashSet

    SnippetVendoringIncludeProcessor(
            Map<String, Object> config,
            File sampleRepoRoot,
            File snippetsOutDir,
            File commonDir) {
        super(config)
        this.sampleRepoRoot = sampleRepoRoot.canonicalFile
        this.snippetsOutDir = snippetsOutDir.canonicalFile
        this.commonDir = commonDir?.canonicalFile
    }

    SnippetVendoringIncludeProcessor(File sampleRepoRoot, File snippetsOutDir, File commonDir) {
        this([:] as Map<String, Object>, sampleRepoRoot, snippetsOutDir, commonDir)
    }

    /**
     * Returns true for every include so the extension sees the full
     * include tree. The {@code commondir} pass-through case is then
     * handled inside {@link #process(Document, PreprocessorReader, String, Map)}.
     */
    @Override
    boolean handles(String target) {
        return true
    }

    /**
     * Read-only view of the manifest accumulated so far. The Gradle
     * task drains this after AsciiDoctor processing completes and
     * serializes it.
     */
    List<ManifestEntry> getManifest() {
        Collections.unmodifiableList(manifestEntries)
    }

    @Override
    void process(Document document, PreprocessorReader reader,
            String target, Map<String, Object> attributes) {

        File resolved = resolveTarget(target, attributes)
        if (resolved == null) {
            // Unresolvable -- fail loudly so the inventory catches it.
            throw new IllegalStateException(
                    "Unresolvable include target '${target}' (no anchor under sample repo root, common dir, or absolute path).")
        }

        if (isUnderSampleRepo(resolved)) {
            String vendoredRel = vendor(resolved)
            String filterLines = attributes['lines']?.toString()
            String filterTag = (attributes['tag'] ?: attributes['tags'])?.toString()
            if (!vendoredAlready.contains(vendoredRel)) {
                manifestEntries << new ManifestEntry(
                        vendoredPath: vendoredRel,
                        upstreamPath: relativeTo(sampleRepoRoot, resolved),
                        filterLines: filterLines,
                        filterTag: filterTag)
                vendoredAlready.add(vendoredRel)
            }
            String rewritten = "../snippets/${vendoredRel}".toString()
            reader.pushInclude(resolved.text, rewritten, rewritten, 1, attributes)
            return
        }

        if (commonDir != null && isUnderCommonDir(resolved)) {
            // Pass through unchanged. The .adoc author kept `{commondir}`
            // syntax; we just resolve it to the on-disk file content.
            reader.pushInclude(resolved.text, target, target, 1, attributes)
            return
        }

        throw new IllegalStateException(
                "Include target '${target}' resolved to '${resolved}', which is outside both " +
                "the sample repo root '${sampleRepoRoot}' and the common dir '${commonDir}'. " +
                'Phase 9 inventory should have caught this.')
    }

    private File resolveTarget(String target, Map<String, Object> attributes) {
        if (!target) return null

        // Absolute paths are taken at face value.
        File absolute = new File(target)
        if (absolute.isAbsolute() && absolute.exists()) {
            return absolute.canonicalFile
        }

        // Relative target -- AsciiDoctor has already expanded {sourceDir},
        // {commondir}, etc. before calling us, so the string we see is
        // either a filesystem-relative path under the sample repo or under
        // the common dir.
        File underSample = new File(sampleRepoRoot, target)
        if (underSample.isFile()) {
            return underSample.canonicalFile
        }
        if (commonDir != null) {
            File underCommon = new File(commonDir, target)
            if (underCommon.isFile()) {
                return underCommon.canonicalFile
            }
        }
        return null
    }

    /**
     * Copies the resolved file under the sample repo into
     * {@link #snippetsOutDir}, preserving the path layout below the
     * top-level sample-app directory ({@code complete/} or
     * {@code initial/}). Returns the relative path under
     * {@code snippets/} as an Asciidoctor-friendly forward-slash string.
     */
    private String vendor(File resolved) {
        String relative = relativeTo(sampleRepoRoot, resolved)
        // Strip the leading 'complete/' or 'initial/' segment so the
        // vendored layout matches the manual fixtures (which drop that
        // prefix).
        String stripped = relative
                .replaceFirst('^complete/', '')
                .replaceFirst('^initial/', '')
        File dest = new File(snippetsOutDir, stripped)
        if (!dest.parentFile.exists()) {
            dest.parentFile.mkdirs()
        }
        if (!dest.exists()) {
            dest.bytes = resolved.bytes
        }
        // Asciidoctor-friendly forward-slash path
        stripped.replace('\\', '/')
    }

    private boolean isUnderSampleRepo(File f) {
        f.canonicalPath.startsWith(sampleRepoRoot.canonicalPath + File.separator) ||
                f.canonicalPath == sampleRepoRoot.canonicalPath
    }

    private boolean isUnderCommonDir(File f) {
        commonDir != null && (
                f.canonicalPath.startsWith(commonDir.canonicalPath + File.separator) ||
                        f.canonicalPath == commonDir.canonicalPath)
    }

    private static String relativeTo(File base, File target) {
        base.toPath().relativize(target.toPath()).toString().replace('\\', '/')
    }
}

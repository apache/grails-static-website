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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import org.yaml.snakeyaml.Yaml

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

/**
 * Validates {@code conf/guides.yml} against the schema documented in
 * the migration design.
 *
 * <p>Three modes, switchable via {@code -PvalidationMode=shape|existence|both}:
 * <ul>
 *   <li>{@code shape} (default) - structure, uniqueness, formats. Does NOT
 *       require {@code sourcePath} to exist on disk. All entries are
 *       checked, even ones not yet migrated.</li>
 *   <li>{@code existence} - of the entries whose {@code sourcePath} directory
 *       exists, validates that each contains at least one {@code .adoc}
 *       file plus {@code toc.yml} and {@code manifest.yml}, and that the
 *       {@code manifest.yml} has the required keys. Entries whose
 *       {@code sourcePath} does not exist are SKIP-warned (not failures).</li>
 *   <li>{@code both} - runs both rule sets.</li>
 * </ul>
 */
@CompileStatic
abstract class ValidateGuidesTask extends DefaultTask {

    public static final String NAME = 'validateGuides'
    public static final String GROUP = 'migration'
    public static final String MODE_SHAPE = 'shape'
    public static final String MODE_EXISTENCE = 'existence'
    public static final String MODE_BOTH = 'both'

    private static final Pattern SHA_40_HEX = ~/^[0-9a-fA-F]{40}$/
    private static final Pattern VERSION_KEY = ~/^[0-9]+$/
    private static final List<String> MANIFEST_REQUIRED_KEYS = [
            'title',
            'subtitle',
            'authors',
            'category',
            'publicationDate',
    ].asImmutable() as List<String>

    @Internal
    final String description = 'Validate conf/guides.yml against the migration schema (-PvalidationMode=shape|existence|both)'

    String group = GROUP

    /** The {@code conf/guides.yml} file under validation. */
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getGuidesYml()

    /**
     * Validation mode. Defaults to {@code shape}; override on the command
     * line via {@code -PvalidationMode=existence} or {@code -PvalidationMode=both}.
     */
    @Input
    abstract Property<String> getMode()

    /**
     * Project root used to resolve {@code sourcePath} entries during
     * existence validation. Defaults to the Gradle project directory.
     */
    @Internal
    abstract DirectoryProperty getProjectRoot()

    static TaskProvider<ValidateGuidesTask> register(Project project) {
        project.tasks.register(NAME, ValidateGuidesTask) { ValidateGuidesTask task ->
            task.guidesYml.convention(project.layout.projectDirectory.file('conf/guides.yml'))
            String requestedMode = (project.findProperty('validationMode') ?: MODE_SHAPE) as String
            task.mode.convention(requestedMode)
            task.projectRoot.convention(project.layout.projectDirectory)
        }
    }

    @CompileDynamic
    @TaskAction
    void validate() {
        String requestedMode = mode.get()
        if (!(requestedMode in [MODE_SHAPE, MODE_EXISTENCE, MODE_BOTH])) {
            throw new GradleException(
                    "Unknown validationMode '${requestedMode}'. " +
                            "Valid values: ${MODE_SHAPE}, ${MODE_EXISTENCE}, ${MODE_BOTH}.")
        }

        File yamlFile = guidesYml.get().asFile
        if (!yamlFile.exists()) {
            throw new GradleException("conf/guides.yml not found at ${yamlFile.absolutePath}")
        }

        Map root = loadYaml(yamlFile)
        if (root == null) {
            throw new GradleException("conf/guides.yml is empty or unreadable")
        }
        if (!root.containsKey('guides')) {
            throw new GradleException("conf/guides.yml is missing the required top-level 'guides' key")
        }
        Object guidesObj = root.get('guides')
        if (!(guidesObj instanceof List)) {
            throw new GradleException("conf/guides.yml 'guides' must be a list, got ${guidesObj?.getClass()?.simpleName}")
        }
        List guides = guidesObj as List

        List<String> errors = []
        List<String> warnings = []

        if (requestedMode == MODE_SHAPE || requestedMode == MODE_BOTH) {
            errors.addAll(validateShape(guides))
        }
        if (requestedMode == MODE_EXISTENCE || requestedMode == MODE_BOTH) {
            File rootDir = projectRoot.get().asFile
            existenceResults(guides, rootDir).each { Map r ->
                List<String> errs = (r['errors'] ?: []) as List<String>
                List<String> warns = (r['warnings'] ?: []) as List<String>
                errors.addAll(errs)
                warnings.addAll(warns)
            }
        }

        warnings.each { logger.warn("[validateGuides] WARN: ${it}") }

        if (errors) {
            String summary = "[validateGuides] ${errors.size()} error(s) in ${yamlFile.absolutePath}"
            errors.eachWithIndex { String err, int idx ->
                logger.error("  ${idx + 1}. ${err}")
            }
            throw new GradleException("${summary} (mode=${requestedMode})")
        }

        int total = guides.size()
        int validated = warnings.findAll { it.contains('SKIP') }.size()
        logger.lifecycle("[validateGuides] mode=${requestedMode}: ${total} guide(s) parsed, " +
                "${validated} SKIP-warned, 0 errors")
    }

    @SuppressWarnings('AssignmentInConditional')
    private Map loadYaml(File file) {
        Yaml yaml = new Yaml()
        file.withReader('UTF-8') { reader ->
            Object result = yaml.load(reader)
            return (result instanceof Map) ? (result as Map) : null
        } as Map
    }

    /**
     * Rule set for {@code shape} mode. Returns a list of error messages
     * (empty list = pass).
     *
     * Marked {@code @CompileDynamic} because the heterogeneous YAML map
     * traversal (mixed {@code Map}/{@code List}/{@code String}/null types)
     * is awkward under {@code @CompileStatic}, especially when GStrings
     * accumulate into a typed {@code List<String>}.
     */
    @CompileDynamic
    @SuppressWarnings('AbcMetric')
    private static List<String> validateShape(List guides) {
        List<String> errors = []
        Set<String> seenNames = new HashSet<>()

        guides.eachWithIndex { Object guideObj, int idx ->
            String prefix = "guides[${idx}]"
            if (!(guideObj instanceof Map)) {
                errors << "${prefix}: must be a mapping, got ${guideObj?.getClass()?.simpleName}"
                return
            }
            Map guide = guideObj as Map

            String name = guide['name'] as String
            if (!name) {
                errors << "${prefix}: missing required 'name'"
            } else if (!seenNames.add(name)) {
                errors << "${prefix}.name: duplicate name '${name}' (already used by an earlier entry)"
            }

            // Required string fields: title, category, publicationDate
            ['title', 'category', 'publicationDate'].each { String field ->
                if (!guide[field]) {
                    errors << "${prefix}: missing required '${field}'"
                }
            }

            // publicationDate must be ISO-8601
            String pubDate = guide['publicationDate'] as String
            if (pubDate && !isIso8601Date(pubDate)) {
                errors << "${prefix}.publicationDate: '${pubDate}' is not a valid ISO-8601 date"
            }

            boolean hasShared = guide.containsKey('shared')
            if (hasShared && !(guide['shared'] instanceof Map)) {
                errors << "${prefix}.shared: must be a mapping if present"
            }

            Object versionsObj = guide['versions']
            if (!(versionsObj instanceof Map) || ((Map) versionsObj).isEmpty()) {
                errors << "${prefix}: missing or empty required 'versions' mapping"
                return
            }
            Map versions = versionsObj as Map

            versions.each { Object versionKeyObj, Object versionObj ->
                String versionKey = String.valueOf(versionKeyObj)
                String vPrefix = "${prefix}.versions['${versionKey}']"

                if (!VERSION_KEY.matcher(versionKey).matches()) {
                    errors << "${vPrefix}: version key '${versionKey}' must be a non-negative integer"
                }
                if (!(versionObj instanceof Map)) {
                    errors << "${vPrefix}: must be a mapping, got ${versionObj?.getClass()?.simpleName}"
                    return
                }
                Map version = versionObj as Map

                String sourcePath = version['sourcePath'] as String
                if (!sourcePath) {
                    errors << "${vPrefix}: missing required 'sourcePath'"
                } else {
                    try {
                        Paths.get(sourcePath)
                    } catch (Exception e) {
                        errors << "${vPrefix}.sourcePath: '${sourcePath}' is not a syntactically valid path: ${e.message}"
                    }
                }

                if (version['extends'] == 'shared' && !hasShared) {
                    errors << "${vPrefix}.extends: 'shared' but parent guide has no 'shared:' block"
                }

                if (version['publicationDate']) {
                    String vd = version['publicationDate'] as String
                    if (!isIso8601Date(vd)) {
                        errors << "${vPrefix}.publicationDate: '${vd}' is not a valid ISO-8601 date"
                    }
                }

                if (version['sampleRef']) {
                    Object sampleRefObj = version['sampleRef']
                    if (!(sampleRefObj instanceof Map)) {
                        errors << "${vPrefix}.sampleRef: must be a mapping if present"
                    } else {
                        Map sampleRef = sampleRefObj as Map
                        if (!sampleRef['repo']) {
                            errors << "${vPrefix}.sampleRef: missing required 'repo'"
                        }
                        if (!sampleRef['branch']) {
                            errors << "${vPrefix}.sampleRef: missing required 'branch'"
                        }
                        String sha = sampleRef['sha'] as String
                        if (!sha) {
                            errors << "${vPrefix}.sampleRef: missing required 'sha'"
                        } else if (!SHA_40_HEX.matcher(sha).matches()) {
                            errors << "${vPrefix}.sampleRef.sha: '${sha}' is not a 40-character hex SHA"
                        }
                    }
                }
            }
        }

        return errors
    }

    /**
     * Rule set for {@code existence} mode. Returns a list of result maps,
     * each with optional {@code errors} and {@code warnings} string lists.
     *
     * Marked {@code @CompileDynamic} for the same reason as
     * {@link #validateShape(List)}.
     */
    @CompileDynamic
    private static List<Map> existenceResults(List guides, File projectRootDir) {
        List<Map> results = []
        guides.eachWithIndex { Object guideObj, int idx ->
            if (!(guideObj instanceof Map)) {
                return
            }
            Map guide = guideObj as Map
            String name = guide['name'] as String
            Object versionsObj = guide['versions']
            if (!(versionsObj instanceof Map)) {
                return
            }
            Map versions = versionsObj as Map

            versions.each { Object versionKeyObj, Object versionObj ->
                if (!(versionObj instanceof Map)) {
                    return
                }
                Map version = versionObj as Map
                String versionKey = String.valueOf(versionKeyObj)
                String sourcePath = version['sourcePath'] as String
                if (!sourcePath) {
                    return
                }
                String prefix = "${name}/v${versionKey}"
                File sourceDir = new File(projectRootDir, sourcePath)
                Map result = [errors: [] as List<String>, warnings: [] as List<String>] as Map

                if (!sourceDir.directory) {
                    (result['warnings'] as List<String>) << "${prefix}: SKIP, sourcePath '${sourcePath}' does not exist on disk yet"
                    results << result
                    return
                }

                File[] adocFiles = new File(sourceDir, 'guide').listFiles { File f -> f.name.endsWith('.adoc') }
                if (!adocFiles || adocFiles.length == 0) {
                    (result['errors'] as List<String>) << "${prefix}: sourcePath '${sourcePath}/guide' contains no .adoc files"
                }

                File tocYml = new File(sourceDir, 'toc.yml')
                if (!tocYml.file) {
                    (result['errors'] as List<String>) << "${prefix}: missing toc.yml at ${tocYml.absolutePath}"
                }

                File manifestYml = new File(sourceDir, 'manifest.yml')
                if (!manifestYml.file) {
                    (result['errors'] as List<String>) << "${prefix}: missing manifest.yml at ${manifestYml.absolutePath}"
                } else {
                    try {
                        Map manifest = manifestYml.withReader('UTF-8') { reader ->
                            Object loaded = new Yaml().load(reader)
                            (loaded instanceof Map) ? (loaded as Map) : null
                        } as Map
                        if (manifest == null) {
                            (result['errors'] as List<String>) << "${prefix}: manifest.yml is empty or not a mapping"
                        } else {
                            MANIFEST_REQUIRED_KEYS.each { String key ->
                                if (!manifest.containsKey(key) || manifest[key] == null) {
                                    (result['errors'] as List<String>) << "${prefix}.manifest.yml: missing required key '${key}'"
                                }
                            }
                        }
                    } catch (Exception e) {
                        (result['errors'] as List<String>) << "${prefix}: manifest.yml is not parseable as YAML: ${e.message}"
                    }
                }

                if (version['extends'] == 'shared' && guide.containsKey('shared')) {
                    Map shared = guide['shared'] as Map
                    String sharedPath = shared['sourcePath'] as String
                    if (sharedPath) {
                        File sharedDir = new File(projectRootDir, sharedPath)
                        if (!sharedDir.directory) {
                            (result['errors'] as List<String>) << "${prefix}.shared.sourcePath: '${sharedPath}' does not exist on disk"
                        } else if ((sharedDir.listFiles()?.length ?: 0) == 0) {
                            (result['errors'] as List<String>) << "${prefix}.shared.sourcePath: '${sharedPath}' exists but is empty"
                        }
                    }
                }

                results << result
            }
        }
        return results
    }

    private static boolean isIso8601Date(String s) {
        try {
            LocalDate.parse(s)
            return true
        } catch (DateTimeParseException ignored) {
            return false
        }
    }
}

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
package website.model.guides

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.yaml.snakeyaml.Yaml

import website.utils.DateUtils

/**
 * Loads the local guide registry ({@code conf/guides.yml}) and produces the
 * flat list of {@link Guide} objects rendered by {@link GuidesPage}.
 *
 * <p>Each top-level YAML entry maps to exactly one {@link Guide}:</p>
 * <ul>
 *   <li>One {@code versions:} child &rarr; {@link SingleGuide}</li>
 *   <li>Two or more {@code versions:} children &rarr; {@link GrailsVersionedGuide}
 *       which renders one link per version (the YAML version key is used
 *       directly as the integer major-version, so the registry's primary key
 *       is stable across Grails major releases).</li>
 * </ul>
 *
 * <p>Guides are identified by their YAML {@code name} field. {@code sampleRef.repo}
 * is treated only as the "Get the Code" target on the rendered guide chrome
 * &mdash; it is NOT used to identify or group guides. Two YAML entries that
 * happen to point at the same external repo are still rendered as two separate
 * guides.</p>
 */
@CompileStatic
class GuidesFetcher {

    private static final String DEFAULT_BRANCH = 'master'

    /**
     * Loads and parses every guide entry in the YAML registry.
     *
     * @param guidesYml the YAML metadata file (typically {@code conf/guides.yml})
     * @param skipFuture if {@code true}, drops guides whose publication date is
     *                   in the future (today + 1)
     * @return list of guides sorted by publication date in descending order
     *         (newest first)
     */
    static List<Guide> fetchGuides(File guidesYml, boolean skipFuture = true) {
        List<Guide> guides = parseGuides(guidesYml)
        if (skipFuture) {
            Date cutoff = tomorrow()
            guides = guides.findAll { Guide g ->
                g.publicationDate != null && g.publicationDate.before(cutoff)
            }
        }
        guides.sort { Guide a, Guide b -> b.publicationDate <=> a.publicationDate }
    }

    /**
     * Walks each top-level YAML guide entry and builds one {@link Guide} per entry.
     *
     * <p>Per-version fields ({@code tags}, {@code sampleRef}, {@code publicationDate})
     * are read from the version block; per-guide fields ({@code title},
     * {@code subtitle}, {@code authors}, {@code category}, {@code publicationDate})
     * are read from the guide entry, with the {@code defaults:} block as fallback.</p>
     */
    @CompileDynamic
    private static List<Guide> parseGuides(File yamlFile) {
        Map root = yamlFile.withReader('UTF-8') { reader ->
            new Yaml().load(reader) as Map
        }
        Map defaults = (root.defaults ?: [:]) as Map
        List entries = (root.guides ?: []) as List

        List<Guide> result = []
        for (Map entry : entries) {
            String name = entry.name as String
            if (!name) {
                continue
            }
            Map<String, Map> validVersions = [:]
            ((entry.versions ?: [:]) as Map).each { Object versionKeyObj, Object versionObj ->
                if (versionObj instanceof Map) {
                    validVersions[versionKeyObj as String] = versionObj as Map
                }
            }
            if (validVersions.isEmpty()) {
                continue
            }

            if (validVersions.size() == 1) {
                Map.Entry<String, Map> only = validVersions.entrySet().iterator().next()
                result << buildSingleGuide(entry, defaults, only.key, only.value)
            } else {
                result << buildVersionedGuide(entry, defaults, validVersions)
            }
        }
        result
    }

    @CompileDynamic
    private static SingleGuide buildSingleGuide(
            Map entry, Map defaults, String versionKey, Map version) {
        Map sampleRef = (version.sampleRef ?: [:]) as Map
        new SingleGuide(
                versionNumber: versionKey,
                authors: (entry.authors ?: defaults.authors ?: []) as List<String>,
                category: (entry.category ?: defaults.category) as String,
                githubSlug: (sampleRef.repo ?: "grails-guides/${entry.name}") as String,
                githubBranch: (sampleRef.branch ?: DEFAULT_BRANCH) as String,
                name: entry.name as String,
                title: entry.title as String,
                subtitle: entry.subtitle as String,
                tags: (version.tags ?: defaults.tags ?: []) as List<String>,
                publicationDate: parsePublicationDate(
                        (version.publicationDate ?: entry.publicationDate) as String)
        )
    }

    /**
     * Builds a {@link GrailsVersionedGuide} from every version block under a
     * single YAML entry. The map key in {@code grailsMayorVersionTags} is the
     * YAML version key parsed as an integer (e.g. {@code '8' -> 8}); non-numeric
     * version keys are silently skipped because the rendered URL slot
     * ({@code /guides/<name>/<major>/...}) requires an integer.
     *
     * <p>The "primary" version surfaced as {@code versionNumber} /
     * {@code githubBranch} / {@code githubSlug} on the guide is the version
     * with the most recent publication date (or the last-iterated version if
     * dates are missing/equal). This drives the "Read More" link in the
     * {@code Latest Guides} sidebar.</p>
     */
    @CompileDynamic
    private static GrailsVersionedGuide buildVersionedGuide(
            Map entry, Map defaults, Map<String, Map> versions) {
        GrailsVersionedGuide guide = new GrailsVersionedGuide(
                authors: (entry.authors ?: defaults.authors ?: []) as List<String>,
                category: (entry.category ?: defaults.category) as String,
                name: entry.name as String,
                title: entry.title as String,
                subtitle: entry.subtitle as String,
        )

        Date latestPubDate = null
        String latestVersionKey = null
        String latestBranch = DEFAULT_BRANCH
        String latestSlug = "grails-guides/${entry.name}"

        versions.each { String versionKey, Map version ->
            if (!versionKey.isInteger()) {
                return
            }
            Integer majorVersion = versionKey.toInteger()
            guide.grailsMayorVersionTags[majorVersion] =
                    (version.tags ?: defaults.tags ?: []) as List<String>

            Date pubDate = parsePublicationDate(
                    (version.publicationDate ?: entry.publicationDate) as String)
            if (pubDate != null && (latestPubDate == null || pubDate.after(latestPubDate))) {
                latestPubDate = pubDate
                latestVersionKey = versionKey
                Map sampleRef = (version.sampleRef ?: [:]) as Map
                latestBranch = (sampleRef.branch ?: DEFAULT_BRANCH) as String
                latestSlug = (sampleRef.repo ?: "grails-guides/${entry.name}") as String
            }
        }

        guide.publicationDate = latestPubDate
        guide.versionNumber = latestVersionKey
        guide.githubBranch = latestBranch
        guide.githubSlug = latestSlug
        guide
    }

    private static Date parsePublicationDate(String dateStr) {
        dateStr ? DateUtils.parseDate(dateStr) : null
    }

    @CompileDynamic
    static Date tomorrow() {
        use(TimeCategory) {
            new Date() + 1.day
        }
    }
}

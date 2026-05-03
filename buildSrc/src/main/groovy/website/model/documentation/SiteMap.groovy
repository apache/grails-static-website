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
package website.model.documentation

import groovy.transform.CompileStatic

import org.yaml.snakeyaml.Yaml

@CompileStatic
class SiteMap {

    static List<ReleaseVersion> versions(File releases) {
        assert releases.exists()
        def model = releases.newInputStream().withCloseable {
            new Yaml().load(it) as Map
        }
        // Accept either `coreReleases:` (canonical) or the legacy `releases:` key
        // for one release cycle so external tooling that still writes the old
        // schema keeps working during the migration window.
        def coreReleases = (model.coreReleases ?: model.releases) as List<Map>
        coreReleases
                .collect { ReleaseVersion.build(it.version as String) }
                .findAll { it != null }
                .toSorted()
    }

    static ReleaseVersion latestVersion(File releases) {
        stableVersions(releases)?.get(0)
    }

    static List<String> olderVersions(File releases) {
        stableVersions(releases).tail()*.versionText
    }

    static List<ReleaseVersion> stableVersions(File releases) {
        versions(releases)
                .findAll { it.getSnapshot() == null }
                .toSorted { a, b -> b <=> a }
    }

    static List<ReleaseVersion> preReleaseVersions(File releases) {
        versions(releases)
                .findAll { it.getSnapshot()?.isMilestone() || it.getSnapshot()?.isReleaseCandidate() }
                .toSorted { a, b -> b <=> a }
    }

    static ReleaseVersion latestPreReleaseVersion(File releases) {
        preReleaseVersions(releases)?.get(0)
    }

    /**
     * Reads the {@code companionArtifacts:} block from {@code conf/releases.yml}
     * and returns the list of companion plugins published for the given Grails
     * major version. Returns an empty list when no entry exists for that major
     * (e.g. Grails 6 and earlier, where companion artifacts weren't tracked,
     * or a future major that hasn't been populated yet).
     *
     * @param releases the {@code conf/releases.yml} file
     * @param major    the Grails major version (e.g. 7, 8)
     * @return immutable list of {@link CompanionArtifact} entries; never null
     */
    static List<CompanionArtifact> companionArtifactsFor(File releases, int major) {
        assert releases.exists()
        def model = releases.newInputStream().withCloseable {
            new Yaml().load(it) as Map
        }
        def section = (model.companionArtifacts ?: [:]) as Map
        def entries = section[major as String] as List<Map>
        if (!entries) {
            return Collections.<CompanionArtifact> emptyList()
        }
        entries.collect { Map e ->
            new CompanionArtifact(
                    artifactId: e.artifactId as String,
                    version: e.version as String,
                    mirrorDirectory: e.mirrorDirectory as String,
                    releaseNotesRepo: e.releaseNotesRepo as String,
                    displayName: e.displayName as String,
            )
        }
    }

    /**
     * @return the highest stable {@link ReleaseVersion} per major version,
     *         keyed by major. Used as the cross-major reference point for
     *         deciding which pre-releases are still relevant (a pre-release
     *         is suppressed once a stable release at or above it has shipped).
     */
    static Map<Integer, ReleaseVersion> highestStablePerMajor(File releases) {
        Map<Integer, ReleaseVersion> result = new LinkedHashMap<Integer, ReleaseVersion>()
        stableVersions(releases).each { ReleaseVersion v ->
            // stableVersions is sorted descending, so the first encounter per
            // major is automatically the highest one for that major.
            if (!result.containsKey(v.major)) {
                result[v.major] = v
            }
        }
        result
    }

    /**
     * @return the highest stable {@link ReleaseVersion} per minor line,
     *         keyed by the {@code "X.Y"} string. Today (with 7.0.0..7.0.10
     *         and 7.1.0 published) this returns
     *         {@code ["7.0": 7.0.10, "7.1": 7.1.0]}. The redesigned downloads
     *         page renders one card per entry, oldest line on the right.
     */
    static Map<String, ReleaseVersion> latestStablePerMinorLine(File releases) {
        Map<String, ReleaseVersion> result = new LinkedHashMap<String, ReleaseVersion>()
        stableVersions(releases).each { ReleaseVersion v ->
            String key = "${v.major}.${v.minor}".toString()
            if (!result.containsKey(key)) {
                result[key] = v
            }
        }
        result
    }

    /**
     * @return the latest pre-release {@link ReleaseVersion} per major, but only
     *         when that pre-release is still relevant (no stable at or above
     *         it has been published). Today this typically returns at most one
     *         entry (the upcoming next-minor RC). Once stable ships, the entry
     *         drops automatically. Apache release policy still requires
     *         /download.html and /documentation.html to link to the released
     *         milestone/RC artefacts - this map drives that section.
     */
    static Map<Integer, ReleaseVersion> latestPreReleasePerMajor(File releases) {
        Map<Integer, ReleaseVersion> highestStables = highestStablePerMajor(releases)
        Map<Integer, ReleaseVersion> result = new LinkedHashMap<Integer, ReleaseVersion>()
        preReleaseVersions(releases).each { ReleaseVersion v ->
            if (result.containsKey(v.major)) {
                return
            }
            ReleaseVersion stableForMajor = highestStables[v.major]
            if (stableForMajor == null || v > stableForMajor) {
                result[v.major] = v
            }
        }
        result
    }

    /**
     * Computes which {@code "X.Y"} minor lines should each get their own card
     * on the home page hero, the downloads page, and the documentation page.
     *
     * <p>Rule (chosen so the slot count adapts to the release cadence rather
     * than a static config):
     * <ul>
     *   <li>Always include every minor line of the highest major that has at
     *       least one stable release. So today, with the highest stable major
     *       being 7 and minor lines 7.0 and 7.1 both published, the result is
     *       {@code ["7.1", "7.0"]} - two slots.</li>
     *   <li>If that highest major has fewer than two minor lines (e.g. just
     *       after 8.0.0 ships), also include every minor line of the previous
     *       major so we don't drop from two cards to one. Once the new major
     *       grows a second minor line of its own (8.1.0), the previous major's
     *       lines roll off automatically.</li>
     * </ul>
     *
     * <p>Returns lines in descending version order
     * ({@code ["8.0", "7.1", "7.0"]} after 8.0.0 ships).
     */
    static List<String> activeMinorLines(File releases) {
        Map<String, ReleaseVersion> latestPerLine = latestStablePerMinorLine(releases)
        if (latestPerLine.isEmpty()) {
            return Collections.<String> emptyList()
        }
        int highestMajor = latestPerLine.values()*.major.max() as int
        List<String> latestMajorLines = latestPerLine.keySet()
                .findAll { it.startsWith("${highestMajor}.") }
                .toSorted { a, b -> b <=> a }
        if (latestMajorLines.size() >= 2) {
            return latestMajorLines
        }
        // Highest major has 0 or 1 minor line; pull in the previous major so
        // the page doesn't shrink to a single card right after a new major
        // ships its first minor.
        List<Integer> majorsWithStables = latestPerLine.values()*.major.unique().toSorted().reverse()
        if (majorsWithStables.size() < 2) {
            return latestMajorLines
        }
        int previousMajor = majorsWithStables[1]
        List<String> previousMajorLines = latestPerLine.keySet()
                .findAll { it.startsWith("${previousMajor}.") }
                .toSorted { a, b -> b <=> a }
        return latestMajorLines + previousMajorLines
    }
}

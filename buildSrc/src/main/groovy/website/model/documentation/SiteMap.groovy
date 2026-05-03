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
}

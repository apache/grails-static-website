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

    static List<SoftwareVersion> versions(File releases) {
        assert releases.exists()
        def model = releases.newInputStream().withCloseable {
            new Yaml().load(it) as Map
        }
        (model.releases as List<Map>)
                .collect { SoftwareVersion.build(it.version as String) }
                .toSorted()
    }

    static SoftwareVersion latestVersion(File releases) {
        stableVersions(releases)?.get(0)
    }

    static List<String> olderVersions(File releases) {
        stableVersions(releases).tail()*.versionText
    }

    static List<SoftwareVersion> stableVersions(File releases) {
        versions(releases)
                .findAll { !it.getIsSnapshot() }
                .toSorted { a, b -> b <=> a }
    }

    static List<SoftwareVersion> preReleaseVersions(File releases) {
        versions(releases)
                .findAll {it.getSnapshot()?.isMilestone() || it.getSnapshot()?.isReleaseCandidate() }
                .toSorted { a, b -> b <=> a }
    }

    static SoftwareVersion latestPreReleaseVersion(File releases) {
        preReleaseVersions(releases)?.get(0)
    }
}

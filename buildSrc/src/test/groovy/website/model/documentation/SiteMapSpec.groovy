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

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

/**
 * Verifies the multi-major helpers introduced for the multi-version downloads
 * /documentation page redesign: latestStablePerMinorLine, latestPreReleasePerMajor,
 * highestStablePerMajor, and activeMinorLines.
 */
class SiteMapSpec extends Specification {

    @TempDir
    Path tempDir

    private File releasesFile(String yaml) {
        File f = tempDir.resolve('releases.yml').toFile()
        f.text = yaml
        f
    }

    void 'latestStablePerMinorLine groups by major.minor and picks highest patch per line'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.0
  - version: 7.0.5
  - version: 7.0.10
  - version: 7.1.0
'''.stripIndent())

        when:
            Map<String, ReleaseVersion> result = SiteMap.latestStablePerMinorLine(releases)

        then:
            result.keySet() as List == ['7.1', '7.0']
            result['7.0'].versionText == '7.0.10'
            result['7.1'].versionText == '7.1.0'
    }

    void 'latestStablePerMinorLine excludes pre-releases'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.0
  - version: 7.1.0-RC1
  - version: 7.1.0
'''.stripIndent())

        when:
            Map<String, ReleaseVersion> result = SiteMap.latestStablePerMinorLine(releases)

        then:
            result['7.1'].versionText == '7.1.0'
            !result.values()*.versionText.any { it.contains('RC') }
    }

    void 'highestStablePerMajor returns one entry per major'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 6.2.3
  - version: 7.0.0
  - version: 7.0.10
  - version: 7.1.0
'''.stripIndent())

        when:
            Map<Integer, ReleaseVersion> result = SiteMap.highestStablePerMajor(releases)

        then:
            result.keySet() as List == [7, 6]
            result[7].versionText == '7.1.0'
            result[6].versionText == '6.2.3'
    }

    void 'latestPreReleasePerMajor includes a pre-release that has not been superseded by stable'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.0
  - version: 7.0.10
  - version: 7.1.0-RC1
'''.stripIndent())

        when:
            Map<Integer, ReleaseVersion> result = SiteMap.latestPreReleasePerMajor(releases)

        then:
            result.containsKey(7)
            result[7].versionText == '7.1.0-RC1'
    }

    void 'latestPreReleasePerMajor suppresses a pre-release once stable has shipped at or above it'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.1.0-RC1
  - version: 7.1.0
'''.stripIndent())

        when:
            Map<Integer, ReleaseVersion> result = SiteMap.latestPreReleasePerMajor(releases)

        then:
            !result.containsKey(7)
    }

    void 'latestPreReleasePerMajor exposes a brand new majors first M/RC even when no stable exists for that major'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.1.0
  - version: 8.0.0-M1
'''.stripIndent())

        when:
            Map<Integer, ReleaseVersion> result = SiteMap.latestPreReleasePerMajor(releases)

        then:
            result.containsKey(8)
            result[8].versionText == '8.0.0-M1'
    }

    void 'activeMinorLines today (highest major has 2 minor lines) returns just those lines'() {

        given: 'today: 7.0.x line plus 7.1.x line shipped, no 8.x stable yet'
            File releases = releasesFile('''
coreReleases:
  - version: 6.2.3
  - version: 7.0.0
  - version: 7.0.10
  - version: 7.1.0
'''.stripIndent())

        when:
            List<String> lines = SiteMap.activeMinorLines(releases)

        then:
            lines == ['7.1', '7.0']
    }

    void 'activeMinorLines after 8.0.0 ships pulls in the previous majors lines so we keep at least two cards'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.10
  - version: 7.1.0
  - version: 8.0.0
'''.stripIndent())

        when:
            List<String> lines = SiteMap.activeMinorLines(releases)

        then:
            lines == ['8.0', '7.1', '7.0']
    }

    void 'activeMinorLines drops the previous majors lines once the new major grows a second minor'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.10
  - version: 7.1.0
  - version: 8.0.0
  - version: 8.1.0
'''.stripIndent())

        when:
            List<String> lines = SiteMap.activeMinorLines(releases)

        then:
            lines == ['8.1', '8.0']
    }

    void 'activeMinorLines returns an empty list when no stable releases exist'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.0-M1
  - version: 7.0.0-RC1
'''.stripIndent())

        when:
            List<String> lines = SiteMap.activeMinorLines(releases)

        then:
            lines == []
    }

    void 'companionArtifactsFor returns an empty list for a major with no entry'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.0
companionArtifacts:
  '7':
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin
'''.stripIndent())

        expect:
            SiteMap.companionArtifactsFor(releases, 8) == []
            SiteMap.companionArtifactsFor(releases, 6) == []
    }

    void 'companionArtifactsFor returns the parsed CompanionArtifact list for a populated major'() {

        given:
            File releases = releasesFile('''
coreReleases:
  - version: 7.0.0
companionArtifacts:
  '7':
    - artifactId: grails-redis
      version: '5.0.1'
      mirrorDirectory: redis
      releaseNotesRepo: apache/grails-redis
      displayName: Grails Redis Plugin
    - artifactId: grails-quartz
      version: '4.0.1'
      mirrorDirectory: quartz
      releaseNotesRepo: apache/grails-quartz
      displayName: Grails Quartz Plugin
'''.stripIndent())

        when:
            List<CompanionArtifact> result = SiteMap.companionArtifactsFor(releases, 7)

        then:
            result.size() == 2
            result[0].artifactId == 'grails-redis'
            result[0].version == '5.0.1'
            result[0].mirrorDirectory == 'redis'
            result[0].releaseNotesRepo == 'apache/grails-redis'
            result[0].displayName == 'Grails Redis Plugin'
            result[1].artifactId == 'grails-quartz'
    }

    void 'versions accepts the legacy releases: key during the migration window'() {

        given:
            File releases = releasesFile('''
releases:
  - version: 7.0.0
  - version: 7.1.0
'''.stripIndent())

        when:
            List<ReleaseVersion> result = SiteMap.versions(releases)

        then:
            result*.versionText == ['7.0.0', '7.1.0']
    }
}

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

/**
 * Verifies that {@link ReleaseVersion} accepts every legacy version format that
 * appears in {@code conf/releases.yml} and that the underlying GrailsVersion
 * delegate orders them correctly. The test cases mirror the historical
 * SoftwareVersionSpec coverage so we keep parity with the parser this class
 * replaces.
 */
class ReleaseVersionSpec extends Specification {

    void 'parses build-snapshot version (#versionText)'() {

        when:
            def v = ReleaseVersion.build(versionText)

        then:
            noExceptionThrown()
            v != null
            v.versionText == versionText
            v.snapshot != null
            v.snapshot.buildSnapshot == isSnapshot
            v.snapshot.releaseCandidate == isReleaseCandidate
            v.snapshot.milestone == isMilestone

        where:
            versionText            || isSnapshot | isReleaseCandidate | isMilestone
            '1.0-SNAPSHOT'         || true       | false              | false
            '1.0.SNAPSHOT'         || true       | false              | false
            '1.0-BUILD-SNAPSHOT'   || true       | false              | false
            '1.0.BUILD-SNAPSHOT'   || true       | false              | false
            '5.0.0-SNAPSHOT'       || true       | false              | false
            '5.0.0-BUILD-SNAPSHOT' || true       | false              | false
            '5.0.0.BUILD-SNAPSHOT' || true       | false              | false
    }

    void 'parses release-candidate version (#versionText)'() {

        when:
            def v = ReleaseVersion.build(versionText)

        then:
            noExceptionThrown()
            v != null
            v.versionText == versionText
            v.snapshot != null
            v.snapshot.buildSnapshot == isSnapshot
            v.snapshot.releaseCandidate == isReleaseCandidate
            v.snapshot.milestone == isMilestone
            v.snapshot.releaseCandidateVersion == rcVersion

        where:
            versionText || isSnapshot | isReleaseCandidate | isMilestone | rcVersion
            '1.0-RC1'   || false      | true               | false       | 1
            '1.0.RC2'   || false      | true               | false       | 2
            '5.0.0-RC1' || false      | true               | false       | 1
            '5.0.0-RC2' || false      | true               | false       | 2
            '5.0.0.RC1' || false      | true               | false       | 1
    }

    void 'parses milestone version (#versionText)'() {

        when:
            def v = ReleaseVersion.build(versionText)

        then:
            noExceptionThrown()
            v != null
            v.versionText == versionText
            v.snapshot != null
            v.snapshot.buildSnapshot == isSnapshot
            v.snapshot.releaseCandidate == isReleaseCandidate
            v.snapshot.milestone == isMilestone
            v.snapshot.milestoneVersion == milestoneVersion

        where:
            versionText || isSnapshot | isReleaseCandidate | isMilestone | milestoneVersion
            '1.0-M1'    || false      | false              | true        | 1
            '1.0.M2'    || false      | false              | true        | 2
            '5.0.0-M1'  || false      | false              | true        | 1
            '5.0.0-M2'  || false      | false              | true        | 2
            '5.0.0.M2'  || false      | false              | true        | 2
    }

    void 'parses 2-part numeric version (#versionText)'() {

        when:
            def v = ReleaseVersion.build(versionText)

        then:
            v != null
            v.versionText == versionText
            v.major == major
            v.minor == minor
            v.patch == 0
            v.snapshot == null

        where:
            versionText || major | minor
            '0.1'       || 0     | 1
            '0.2'       || 0     | 2
            '1.0'       || 1     | 0
    }

    void 'parses 3-part stable version (#versionText)'() {

        when:
            def v = ReleaseVersion.build(versionText)

        then:
            v != null
            v.versionText == versionText
            v.major == major
            v.minor == minor
            v.patch == patch
            v.snapshot == null

        where:
            versionText || major | minor | patch
            '7.0.0'     || 7     | 0     | 0
            '7.0.1'     || 7     | 0     | 1
            '7.1.0'     || 7     | 1     | 0
            '6.2.3'     || 6     | 2     | 3
    }

    void 'compareTo orders milestone < rc < final'() {
        expect:
            ReleaseVersion.build('1.0.M1') < ReleaseVersion.build('1.0.RC1')
            ReleaseVersion.build('1.0.RC1') < ReleaseVersion.build('1.0')
    }

    void 'compareTo orders 7.0.0-M1 < 7.0.0-RC1 < 7.0.0'() {
        expect:
            ReleaseVersion.build('7.0.0-M1') < ReleaseVersion.build('7.0.0-RC1')
            ReleaseVersion.build('7.0.0-RC1') < ReleaseVersion.build('7.0.0')
    }

    void 'compareTo orders patches correctly'() {
        expect:
            ReleaseVersion.build('7.0.0') < ReleaseVersion.build('7.0.1')
            ReleaseVersion.build('7.0.1') < ReleaseVersion.build('7.1.0')
            ReleaseVersion.build('7.1.0') < ReleaseVersion.build('8.0.0')
    }

    void 'preserves original text for display fidelity'() {

        expect:
            ReleaseVersion.build(input).versionText == input

        where:
            input << ['0.1', '1.0', '1.0.RC1', '3.0.0.M1', '7.0.0-RC2', '7.1.0']
    }

    void 'returns null for unparseable input (#input)'() {

        expect:
            ReleaseVersion.build(input) == null

        where:
            input << [null, '', '   ', 'not-a-version', 'abc.def.ghi']
    }
}

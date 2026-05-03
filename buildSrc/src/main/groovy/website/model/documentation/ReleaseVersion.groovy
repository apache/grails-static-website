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

import java.util.regex.Pattern

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.grailsversion.GrailsVersion
import org.grails.datastore.mapping.core.grailsversion.Snapshot

/**
 * A Grails release version backed by the canonical {@link GrailsVersion} class
 * from apache/grails-core. This thin facade exists for two reasons:
 *
 * <ol>
 *   <li><strong>Display fidelity</strong>: {@link GrailsVersion} stores whatever
 *       string was passed to its constructor as {@code versionText}, but its
 *       constructor only accepts 3-or-4-part versions. Legacy entries in
 *       {@code conf/releases.yml} include 2-part versions ({@code "0.1"},
 *       {@code "1.0"}), 3-part qualifier-only versions ({@code "1.0.RC1"}),
 *       and 4-part dot-qualifier versions ({@code "3.0.0.M1"}). This class
 *       normalizes those into a form {@link GrailsVersion} accepts before
 *       construction while preserving the original text for display in
 *       dropdowns and URL building.</li>
 *   <li><strong>Single source of truth</strong>: parsing and comparison rules
 *       now live in grails-core. This codebase no longer maintains its own
 *       regex-based version model.</li>
 * </ol>
 */
@CompileStatic
class ReleaseVersion implements Comparable<ReleaseVersion> {

    private static final Pattern NUMERIC = ~/^\d+$/

    /** The original, un-normalized version text exactly as it appears in releases.yml. */
    final String versionText

    /** The normalized {@link GrailsVersion} that handles all parsing and comparison logic. */
    final GrailsVersion delegate

    private ReleaseVersion(String versionText, GrailsVersion delegate) {
        this.versionText = versionText
        this.delegate = delegate
    }

    /**
     * Parses a Grails-flavoured version string. Accepts every format that the
     * legacy {@code SoftwareVersion} accepted plus everything {@link GrailsVersion}
     * natively supports. Returns {@code null} if the string is null, empty, or
     * unparseable.
     *
     * <p>Normalizations applied (input → form passed to GrailsVersion):
     * <pre>
     *   "0.1"             → "0.1.0"             (2-part numeric)
     *   "1.0-RC1"         → "1.0.0-RC1"         (2-part with qualifier)
     *   "1.0-SNAPSHOT"    → "1.0.0-SNAPSHOT"    (2-part with snapshot)
     *   "1.0.RC1"         → "1.0.0-RC1"         (3-part qualifier-only)
     *   "1.0.SNAPSHOT"    → "1.0.0-SNAPSHOT"    (3-part snapshot-only)
     *   "3.0.0.M1"        → "3.0.0-M1"          (4-part dot-qualifier; also
     *                                            sidesteps a latent bug in
     *                                            GrailsVersion's 4-part branch
     *                                            where the patch component
     *                                            never gets assigned)
     * </pre>
     *
     * @param version the input version string, possibly trimmable
     * @return a ReleaseVersion, or {@code null} if the input cannot be parsed
     */
    static ReleaseVersion build(String version) {
        if (!version) {
            return null
        }
        String original = version.trim()
        if (!original) {
            return null
        }
        String normalized = normalize(original)
        try {
            return new ReleaseVersion(original, new GrailsVersion(normalized))
        } catch (IllegalArgumentException ignored) {
            return null
        } catch (NumberFormatException ignored) {
            return null
        }
    }

    private static String normalize(String version) {
        String[] parts = version.split('\\.')
        switch (parts.length) {
            case 2:
                if (parts[1].contains('-')) {
                    // "X.Y-QUALIFIER" → "X.Y.0-QUALIFIER" (e.g. "1.0-RC1", "1.0-SNAPSHOT").
                    String[] subparts = parts[1].split('-', 2)
                    return "${parts[0]}.${subparts[0]}.0-${subparts[1]}".toString()
                }
                // "X.Y" → "X.Y.0" (e.g. "0.1", "1.0").
                return "${version}.0".toString()
            case 3:
                if (parts[2].contains('-')) {
                    String[] sub = parts[2].split('-', 2)
                    if (!NUMERIC.matcher(sub[0]).matches()) {
                        // "X.Y.QUAL-MORE" → "X.Y.0-QUAL-MORE" (e.g. "1.0.BUILD-SNAPSHOT")
                        // where the first sub-part is itself a qualifier word, not a patch.
                        return "${parts[0]}.${parts[1]}.0-${parts[2]}".toString()
                    }
                    // "X.Y.Z-QUALIFIER" - GrailsVersion handles this natively via its
                    // own dash-splitting branch.
                    return version
                }
                if (!NUMERIC.matcher(parts[2]).matches()) {
                    // "X.Y.QUALIFIER" → "X.Y.0-QUALIFIER" (e.g. "1.0.RC1", "1.0.SNAPSHOT").
                    return "${parts[0]}.${parts[1]}.0-${parts[2]}".toString()
                }
                return version
            case 4:
                // "X.Y.Z.QUALIFIER" → "X.Y.Z-QUALIFIER" (e.g. "3.0.0.M1").
                // Also sidesteps an upstream bug in GrailsVersion's 4-part branch
                // where it sets snapshot but forgets to assign patch.
                return "${parts[0]}.${parts[1]}.${parts[2]}-${parts[3]}".toString()
            default:
                return version
        }
    }

    int getMajor() { delegate.major }

    int getMinor() { delegate.minor }

    int getPatch() { delegate.patch }

    /**
     * @return the qualifier (Milestone/RC/Snapshot) or {@code null} if this is a
     *         pure {@code X.Y.Z} stable release. Callers can branch on this
     *         using Groovy truth ({@code if (version.snapshot)}) or an explicit
     *         {@code != null} check.
     */
    Snapshot getSnapshot() { delegate.getSnapshot() }

    @Override
    int compareTo(ReleaseVersion o) {
        delegate <=> o.delegate
    }

    @Override
    boolean equals(Object o) {
        if (this.is(o)) {
            return true
        }
        if (!(o instanceof ReleaseVersion)) {
            return false
        }
        return versionText == ((ReleaseVersion) o).versionText
    }

    @Override
    int hashCode() {
        versionText.hashCode()
    }

    @Override
    String toString() {
        versionText
    }
}

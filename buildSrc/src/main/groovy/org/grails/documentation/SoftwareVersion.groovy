package org.grails.documentation

import groovy.transform.CompileStatic

@CompileStatic
class SoftwareVersion implements Comparable<SoftwareVersion> {

    int major
    int minor
    int patch

    Snapshot snapshot

    String versionText

    /**
     * Parses common Grails version formats.
     * Examples:
     *  - 1.0 / 1.0.0
     *  - 1.0-RC1 / 1.0.RC1 /1.0.0-RC1 / 1.0.0.RC1
     *  - 1.0.M1 / 1.0.0-M1
     *  - 1.0.0-SNAPSHOT / 1.0.0.BUILD-SNAPSHOT
     */
    static SoftwareVersion build(String version) {
        if (!version) {
            return null
        }

        def v = version.trim()
        def softVersion = new SoftwareVersion(versionText: v)

        // Match: major.minor[.patch][separator qualifier]
        // qualifier may be after '-' or '.' (e.g. 1.0-RC1 or 1.0.RC1)
        // patch is optional (e.g. 1.0.RC1)
        def m = (v =~ /^(\d+)\.(\d+)(?:\.(\d+))?(?:[.-](.+))?$/)
        if (!m.matches()) {
            return null
        }

        softVersion.major = m.group(1).toInteger()
        softVersion.minor = m.group(2).toInteger()

        def patchStr = m.group(3)
        def qualifier = m.group(4)

        // If patch is missing but the third segment is actually a qualifier (1.0.RC1),
        // the regex puts it in qualifier and leaves patch null.
        softVersion.patch = patchStr ? patchStr.toInteger() : 0

        if (qualifier) {
            softVersion.snapshot = new Snapshot(qualifier)
        }

        return softVersion
    }

    boolean isSnapshot() {
        snapshot != null
    }

    @Override
    int compareTo(SoftwareVersion o) {
        int majorCompare = this.major <=> o.major
        if (majorCompare != 0) {
            return majorCompare
        }

        int minorCompare = this.minor <=> o.minor
        if (minorCompare != 0) {
            return minorCompare
        }

        int patchCompare = this.patch <=> o.patch
        if (patchCompare != 0) {
            return patchCompare
        }

        if (this.isSnapshot() && !o.isSnapshot()) {
            return -1
        } else if (!this.isSnapshot() && o.isSnapshot()) {
            return 1
        } else if (this.isSnapshot() && o.isSnapshot()) {
            return this.getSnapshot() <=> o.getSnapshot()
        } else {
            return 0
        }
    }
}

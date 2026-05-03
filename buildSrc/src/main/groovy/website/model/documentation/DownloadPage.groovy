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
package website.model.documentation

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import static website.utils.RenderUtils.renderHtml

@CompileStatic
class DownloadPage {

    /** First Grails major distributed via the Apache mirrors (rather than GitHub releases). */
    private static final int FIRST_APACHE_MAJOR = 7

    static String binaryUrl(String version, String artifact = 'grails', String ext = '', String directory = 'core') {
        "https://www.apache.org/dyn/closer.lua/grails/$directory/$version/distribution/apache-$artifact-$version-bin.zip$ext?action=download"
    }

    static String sourceUrl(String version, String artifact = 'grails', String ext = '', String directory = 'core') {
        "https://www.apache.org/dyn/closer.lua/grails/$directory/$version/sources/apache-$artifact-$version-src.zip$ext?action=download"
    }

    static String binaryVerificationUrl(String version, String artifact = 'grails', String ext = '', String directory = 'core') {
        "https://downloads.apache.org/grails/$directory/$version/distribution/apache-$artifact-$version-bin.zip$ext"
    }

    static String sourceVerificationUrl(String version, String artifact = 'grails', String ext = '', String directory = 'core') {
        "https://downloads.apache.org/grails/$directory/$version/sources/apache-$artifact-$version-src.zip$ext"
    }

    /**
     * Does not handle pre-release versions as these are not displayed in the select box.
     */
    static String resolveOldDownloadUrl(String version) {
        def parts = ((version.split(/\./)*.replaceAll(/\D.*/, '')*.toInteger()) + [0, 0, 0]).take(3)
        def (major, minor, patch) = [parts[0], parts[1], parts[2]]
        def tag = "v$version"
        if (major < FIRST_APACHE_MAJOR) {
            def baseUrl = 'https://github.com/apache/grails-core/releases/download'
            def artifactName = "grails-$version"
            if (major == 6) {
                baseUrl = 'https://github.com/apache/grails-forge/releases/download'
                artifactName = "grails-cli-$version"
            }
            if (major == 1 && minor == 1) {
                artifactName = "grails-bin-$version"
            }
            if (major <= 1 && patch == 0) {
                tag = "v$major.$minor"
            }
            return "$baseUrl/$tag/${artifactName}.zip"
        }
        else {
            return "https://www.apache.org/dyn/closer.lua/grails/core/$version/distribution/apache-grails-$version-bin.zip?action=download"
        }
    }

    /**
     * Renders the download card for a single version. For Apache-distributed
     * versions ({@code major >= 7}), this is Source + Binary + Binary Wrapper
     * with full sha512/asc verification links plus one entry per supplied
     * {@link CompanionArtifact}. For older GitHub-released versions, only the
     * single CLI binary link is rendered. Snapshots render an empty card.
     *
     * @param version    the Grails version string (or the literal {@code "snapshot"}).
     * @param companions per-major companion plugins to render under the core
     *                   download links. Empty for non-Apache versions or for
     *                   majors that don't yet have companions tracked.
     */
    @CompileDynamic
    static String renderDownload(String version, List<CompanionArtifact> companions = []) {

        if (version.toLowerCase().contains('snapshot')) {
            return ''
        }

        boolean isApacheDistributed = isApacheDistributed(version)

        renderHtml {
            div(class: 'guide-group') {
                if (version) {
                    div(class: 'guide-group-header') {
                        img(src: '[%url]/images/download.svg', alt: "Download Grails ($version)")
                        h2(
                                DocumentationPage.resolveDocumentationName(version)
                        )
                    }
                    ul {
                        if (isApacheDistributed) {
                            li {
                                a(href: DownloadPage.sourceUrl(version), 'Source')
                                a(href: sourceVerificationUrl(version, 'grails', '.sha512'), 'SHA512')
                                a(href: sourceVerificationUrl(version, 'grails', '.asc'), 'ASC')
                            }
                            li {
                                a(href: binaryUrl(version, 'grails'), 'Binary')
                                a(href: binaryVerificationUrl(version, 'grails', '.sha512'), 'SHA512')
                                a(href: binaryVerificationUrl(version, 'grails', '.asc'), 'ASC')
                            }
                            li {
                                a(href: binaryUrl(version, 'grails-wrapper'), 'Binary Wrapper')
                                a(href: binaryVerificationUrl(version, 'grails-wrapper', '.sha512'), 'SHA512')
                                a(href: binaryVerificationUrl(version, 'grails-wrapper', '.asc'), 'ASC')
                            }
                            companions.each { CompanionArtifact c ->
                                li {
                                    a(
                                            href: sourceUrl(c.version, c.artifactId, '', c.mirrorDirectory),
                                            "${c.displayName} ${c.version} Source"
                                    )
                                    a(
                                            href: sourceVerificationUrl(c.version, c.artifactId, '.sha512', c.mirrorDirectory),
                                            'SHA512'
                                    )
                                    a(
                                            href: sourceVerificationUrl(c.version, c.artifactId, '.asc', c.mirrorDirectory),
                                            'ASC'
                                    )
                                }
                            }
                        } else {
                            li {
                                a(
                                        href: "https://github.com/apache/grails-forge/releases/download/v$version/grails-cli-${version}.zip",
                                        'Binary'
                                )
                            }
                        }
                        li {
                            a(
                                    href: "https://github.com/apache/grails-core/releases/tag/v$version",
                                    'Grails Release Notes')
                        }
                        if (isApacheDistributed) {
                            companions.each { CompanionArtifact c ->
                                li {
                                    a(
                                            href: "https://github.com/${c.releaseNotesRepo}/releases/tag/v${c.version}",
                                            "${c.displayName} ${c.version} Release Notes"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @return {@code true} if this version line is distributed through the
     *         Apache mirrors ({@code major >= 7}). Older versions fell back to
     *         GitHub releases on apache/grails-core or apache/grails-forge.
     */
    private static boolean isApacheDistributed(String version) {
        ReleaseVersion parsed = ReleaseVersion.build(version)
        if (parsed == null) {
            // Defensive: if we can't parse the version, infer from the leading digit
            // the way the legacy code did. Keeps behavior identical for any unusual
            // input that slips through.
            return version.startsWith('7') || version.startsWith('8') || version.startsWith('9')
        }
        return parsed.major >= FIRST_APACHE_MAJOR
    }

    /**
     * Renders the Downloads page in a multi-version card-grid layout. The
     * legacy two-column-stack layout collapsed every release into a single
     * column on the left; this layout has a top-level "Current Releases"
     * grid (one card per active minor line, e.g. 7.0 and 7.1 today, plus
     * 8.0 once that ships), a "Pre-release" grid below it for any
     * Apache-released milestones / RCs that haven't been superseded by
     * stable, the older-versions dropdown, and a "Get Started" two-column
     * footer with Application Forge + SDKMAN install instructions.
     */
    @CompileDynamic
    static String mainContent(File releases) {
        List<String> currentLines = SiteMap.activeMinorLines(releases)
        Map<String, ReleaseVersion> latestPerLine = SiteMap.latestStablePerMinorLine(releases)
        Map<Integer, ReleaseVersion> preReleasesPerMajor = SiteMap.latestPreReleasePerMajor(releases)

        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    h1('Downloads')
                }
            }
            div(class: 'content') {
                p(class: 'release-page-intro') {
                    mkp.yieldUnescaped(
                            'We provide OpenPGP signatures (\'.asc\') files and checksums (\'.sha512\') for ' +
                            'every release artifact. We recommend that you '
                    )
                    a(href: 'https://www.apache.org/info/verification.html', 'verify')
                    mkp.yieldUnescaped(
                            ' the integrity of downloaded files by generating your own checksums and match ' +
                            'them against ours, and checking signatures using the '
                    )
                    a(href: 'https://www.apache.org/dyn/closer.lua/grails/KEYS?action=download', 'KEYS')
                    mkp.yieldUnescaped(' file which contains the Grails OpenPGP release keys.')
                }

                h2(class: 'release-section-header column-header', 'Current Releases')
                if (currentLines.isEmpty()) {
                    p('No stable releases have been recorded yet.')
                } else {
                    div(class: 'release-grid') {
                        currentLines.each { String lineKey ->
                            ReleaseVersion v = latestPerLine[lineKey]
                            if (v != null) {
                                mkp.yieldUnescaped(
                                        DownloadPage.renderDownload(
                                                v.versionText,
                                                SiteMap.companionArtifactsFor(releases, v.major)
                                        )
                                )
                            }
                        }
                    }
                }

                if (!preReleasesPerMajor.isEmpty()) {
                    h2(class: 'release-section-header column-header', 'Pre-release (Apache-released)')
                    p(
                            'Per Apache release policy, the milestones and release candidates below are ' +
                            'official Apache releases published to Maven Central with full source, binary, ' +
                            'and signature artifacts. They are not featured on the home page.'
                    )
                    div(class: 'release-grid') {
                        preReleasesPerMajor.values().each { ReleaseVersion v ->
                            mkp.yieldUnescaped(
                                    DownloadPage.renderDownload(
                                            v.versionText,
                                            SiteMap.companionArtifactsFor(releases, v.major)
                                    )
                            )
                        }
                    }
                }

                h2(class: 'release-section-header column-header', 'Older Versions')
                p('You can download previous versions as far back as Grails 0.1.')
                p(
                        'NOTE: Versions prior to 7.0.0-M4 are not ASF releases. Links to those releases are ' +
                        'provided here as a convenience.'
                )
                div(class: 'version-selector') {
                    select(class: 'form-control', onchange: 'window.location.href = this.value') {
                        option(label: 'Select a version', disabled: 'disabled', selected: 'selected')
                        SiteMap.stableVersions(releases)*.versionText.each {
                            option(value: DownloadPage.resolveOldDownloadUrl(it), it)
                        }
                    }
                }

                h2(class: 'release-section-header column-header', 'Get Started')
                div(class: 'two-columns') {
                    div(class: 'odd column') {
                        h3(
                                class: 'column-header',
                                style: 'margin-bottom: 10px',
                                'Grails Application Forge'
                        )
                        p('The quickest way to get started with our application generator:')
                        p {
                            a(href: 'https://start.grails.org', 'Grails Application Forge')
                        }
                    }
                    div(class: 'column') {
                        h3(
                                class: 'column-header',
                                style: 'margin-bottom: 10px',
                                'Installing with SDKMAN!'
                        )
                        p {
                            a(
                                    href: 'https://sdkman.io/',
                                    'SDKMAN! (The Software Development Kit Manager)'
                            )
                        }
                        p(
                                'This tool makes installing the Grails framework on any Unix based platform ' +
                                '(Mac OSX, Linux, Cygwin, Solaris, or FreeBSD) easy.'
                        )
                        p('Simply open a new terminal and enter:')
                        div(class: 'code', '$ curl -s https://get.sdkman.io | bash')
                        p('Follow the on-screen instructions to complete installation.')
                        p('Open a new terminal or type the command:')
                        div(class: 'code', '$ source "$HOME/.sdkman/bin/sdkman-init.sh"')
                        p('Then install the latest stable Grails version:')
                        div(class: 'code', '$ sdk install grails')
                        p(
                                'If prompted, make this your default version. After installation is complete ' +
                                'it can be tested with:'
                        )
                        div(class: 'code', '$ grails --version')
                        p('That\'s all there is to it!')
                    }
                }
            }
        }
    }
}

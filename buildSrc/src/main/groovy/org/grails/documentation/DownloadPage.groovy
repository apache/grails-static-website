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
package org.grails.documentation

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

@CompileStatic
class DownloadPage {

    static String binaryUrl(String version, String artifact, String ext = '', String directory = 'core') {
        "https://www.apache.org/dyn/closer.lua/grails/core/${directory}/${version}/distribution/apache-${artifact}-${version}-incubating-bin.zip${ext}?action=download"

    }

    static String sourceUrl(String version, String ext = '', String artifact='grails', String directory = 'core') {
        "https://www.apache.org/dyn/closer.lua/grails/${directory}/${version}/sources/apache-${artifact}-${version}-incubating-src.zip${ext}?action=download"
    }

    @CompileDynamic
    static String renderDownload(String version) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)
        boolean isSnapshot = version.endsWith('-SNAPSHOT') || version.contains('snapshot')
        if (!isSnapshot) {
            html.div(class: "guidegroup") {
            if (version) {
                div(class: "guidegroupheader") {
                    img(src: "[%url]/images/download.svg", alt: "Download Grails (${version})")
                    h2 "${isSnapshot ? 'Snapshot' : (version.contains('-M') ? 'Milestone' : (version.contains('-RC') ? 'Release Candidate': 'Latest Stable'))} Version (${version}) Downloads"
                }
                ul {
                    if (version.startsWith('7')) {
                        li {
                            a(href: sourceUrl(version), 'Source')
                            a(href: sourceUrl(version, '.sha512'), 'SHA512')
                            a(href: sourceUrl(version, '.asc'), 'ASC')
                        }
                        li {
                            a(href: binaryUrl(version, 'grails'), 'Binary')
                            a(href: binaryUrl(version, 'grails', '.sha512'), 'SHA512')
                            a(href: binaryUrl(version, 'grails', '.asc'), 'ASC')
                        }
                        li {
                            a(href: binaryUrl(version, 'grails-wrapper'), 'Binary Wrapper')
                            a(href: binaryUrl(version, 'grails-wrapper', '.sha512'), 'SHA512')
                            a(href: binaryUrl(version, 'grails-wrapper', '.asc'), 'ASC')
                        }
                        li {
                            a(href: sourceUrl(version, '','grails-spring-security', 'spring-security'), 'Grails Spring Security Plugin Source')
                            a(href: sourceUrl(version, '.sha512','grails-spring-security', 'spring-security'), 'SHA512')
                            a(href: sourceUrl(version, '.asc','grails-spring-security', 'spring-security'), 'ASC')
                        }
                        li {
                            a(href: sourceUrl('5.0.0-M4', '','grails-redis',  'redis'), 'Grails Redis 5.0.0-M4 Plugin Source')
                            a(href: sourceUrl('5.0.0-M4', '.sha512', 'grails-redis', 'redis'), 'SHA512')
                            a(href: sourceUrl('5.0.0-M4', '.asc','grails-redis', 'redis'), 'ASC')
                        }
                    } else {
                        li {
                            a(href: "https://github.com/apache/grails-forge/releases/download/v${version}/grails-cli-${version}.zip", 'Binary')
                        }
                    }
                    li {
                        a(href: "https://github.com/apache/grails-core/releases/tag/v${version}", 'Grails Release Notes')
                    }
                    li {
                        a(href: "https://github.com/apache/grails-spring-security/releases/tag/v${version}", 'Grails Spring Security Plugin Release Notes')
                    }
                    li {
                        a(href: "https://github.com/apache/grails-redis/releases/tag/v5.0.0-M4", 'Grails Redis 5.0.0-M4 Plugin Release Notes')
                    }
                }
            }
            }
        }
        writer.toString()
    }


    @CompileDynamic
    static String mainContent(File releases) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)

        html.div(class: 'headerbar chalicesbg') {
            html.div(class: 'content') {
                h1 'Downloads'
            }
        }
        SoftwareVersion preRelease = SiteMap.latestPreReleaseVersion(releases)
        SoftwareVersion latest = SiteMap.latestVersion(releases)
        html.div(class: 'content') {
            html.div(class: "twocolumns") {
                html.div(class: "odd column"){
                    h3(class: 'columnheader', style: 'margin-bottom: 10px;', 'Source and Binary Releases')
                    p 'NOTE: Versions prior to 7.0.0-M4 are not ASF releases. Links to those releases are provided here as a convenience.'
                    mkp.yieldUnescaped "We provide OpenPGP signatures ('.asc') files and checksums ('.sha512') for every release artifact. We recommend that you "
                    a(href: 'https://www.apache.org/info/verification.html', 'verify')
                    mkp.yieldUnescaped " the integrity of downloaded files by generating your own checksums and match them against ours, and checking signatures using the "
                    a(href: 'https://www.apache.org/dyn/closer.lua/grails/KEYS?action=download', 'KEYS')
                    mkp.yieldUnescaped " file which contains the Grails OpenPGP release keys."
                    p ''
                    if (preRelease > latest) {
                        mkp.yieldUnescaped(renderDownload(preRelease.versionText))
                    }

                    mkp.yieldUnescaped(renderDownload('snapshot'))

                    mkp.yieldUnescaped(renderDownload(latest.versionText))

                    h3(class:'columnheader', 'Older Versions')
                    p 'You can download previous versions as far back as Grails 1.2.0.'
                    div(class:'versionselector') {
                        select(class:'form-control', onchange:"window.location.href=this.value.startsWith('6') ? 'https://github.com/apache/grails-forge/releases/download/v'+this.value+'/grails-cli-'+this.value+'.zip': 'https://github.com/apache/grails-core/releases/download/v'+this.value+'/grails-'+this.value+'.zip'") {
                            option(label:'Select a version', disabled:'disabled', selected:'selected')
                            SiteMap.stableVersions(releases)*.versionText.each {
                                option(value: it, it)
                            }
                        }
                    }
                }
                html.div(class: "column") {

                    h3(class: 'columnheader', style: 'margin-bottom: 10px;', 'Other ways to get Grails')

                    mkp.yieldUnescaped'Select a profile and set of features tailored to your needs with our application initializer:'
                    a(href: 'https://start.grails.org', 'Grails Application Forge')


                    h3(class: 'columnheader', style: 'margin-bottom: 10px;', 'Installing with SDKMAN!')

                    p {
                        a(href: 'https://sdkman.io/', 'SDKMAN! (The Software Development Kit Manager)')
                    }

                    p 'This tool makes installing the Grails framework on any Unix based platform (Mac OSX, Linux, Cygwin, Solaris, or FreeBSD) easy.'
                    p 'Simply open a new terminal and enter:'
                    div(class:'code', '$ curl -s https://get.sdkman.io | bash')
                    p 'Follow the on-screen instructions to complete installation.'
                    p 'Open a new terminal or type the command:'
                    div(class:'code', '$ source "$HOME/.sdkman/bin/sdkman-init.sh"')
                    p 'Then install the latest stable Grails version:'
                    div(class:'code', '$ sdk install grails')
                    p 'If prompted, make this your default version. After installation is complete it can be tested with:'
                    div(class:'code', '$ grails --version')
                    p "That's all there is to it!"
                }
            }
        }

        writer.toString()
    }

}

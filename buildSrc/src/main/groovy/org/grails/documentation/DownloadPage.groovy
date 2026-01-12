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

    static String binaryUrl(String version, String artifact = 'grails', String ext = '', String directory = 'core') {
        "https://www.apache.org/dyn/closer.lua/grails/${directory}/${version}/distribution/apache-${artifact}-${version}-bin.zip${ext}?action=download"

    }

    static String sourceUrl(String version, String artifact = 'grails', String ext = '', String directory = 'core') {
        "https://www.apache.org/dyn/closer.lua/grails/${directory}/${version}/sources/apache-${artifact}-${version}-src.zip${ext}?action=download"
    }

    @CompileDynamic
    static String renderDownload(String version) {
        String redisVersion = '5.0.0'
        String quartzVersion = '4.0.0'
        String springSecurityVersion = '7.0.0'
        String grailsGithubActionsVersion = '1.0.1'
        String grailsGradlePublishVersion = '0.0.4'

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
                                a(href: sourceUrl(version, 'grails', '.sha512'), 'SHA512')
                                a(href: sourceUrl(version, 'grails', '.asc'), 'ASC')
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
                                a(href: sourceUrl(springSecurityVersion, 'grails-spring-security', '', 'spring-security'), "Grails Spring Security ${springSecurityVersion} Plugin Source")
                                a(href: sourceUrl(springSecurityVersion, 'grails-spring-security', '.sha512', 'spring-security'), 'SHA512')
                                a(href: sourceUrl(springSecurityVersion, 'grails-spring-security', '.asc', 'spring-security'), 'ASC')
                            }
                            li {
                                a(href: sourceUrl(redisVersion, 'grails-redis', '', 'redis'), "Grails Redis ${redisVersion} Plugin Source")
                                a(href: sourceUrl(redisVersion, 'grails-redis', '.sha512', 'redis'), 'SHA512')
                                a(href: sourceUrl(redisVersion, 'grails-redis', '.asc', 'redis'), 'ASC')
                            }
                            li {
                                a(href: sourceUrl(quartzVersion, 'grails-quartz', '', 'quartz'), "Grails Quartz ${quartzVersion} Plugin Source")
                                a(href: sourceUrl(quartzVersion, 'grails-quartz', '.sha512', 'quartz'), 'SHA512')
                                a(href: sourceUrl(quartzVersion, 'grails-quartz', '.asc', 'quartz'), 'ASC')
                            }
                            li {
                                a(href: sourceUrl(grailsGithubActionsVersion, 'grails-github-actions', '', 'actions'), "Grails GitHub Actions ${grailsGithubActionsVersion} Source")
                                a(href: sourceUrl(grailsGithubActionsVersion, 'grails-github-actions', '.sha512', 'actions'), 'SHA512')
                                a(href: sourceUrl(grailsGithubActionsVersion, 'grails-github-actions', '.asc', 'actions'), 'ASC')
                            }
                            li {
                                a(href: sourceUrl(grailsGradlePublishVersion, 'grails-publish', '', 'grails-publish'), "Grails Publish Gradle Plugin ${grailsGradlePublishVersion} Source")
                                a(href: sourceUrl(grailsGradlePublishVersion, 'grails-publish', '.sha512', 'grails-publish'), 'SHA512')
                                a(href: sourceUrl(grailsGradlePublishVersion, 'grails-publish', '.asc', 'grails-publish'), 'ASC')
                            }
                        } else {
                            li {
                                a(href: "https://github.com/apache/grails-forge/releases/download/v${version}/grails-cli-${version}.zip", 'Binary')
                            }
                        }
                        li {
                            a(href: "https://github.com/apache/grails-core/releases/tag/v${version}", 'Grails Release Notes')
                        }
                        if (version.startsWith('7')) {
                            li {
                                a(href: "https://github.com/apache/grails-spring-security/releases/tag/v${springSecurityVersion}", "Grails Spring Security Plugin ${springSecurityVersion} Release Notes")
                            }
                            li {
                                a(href: "https://github.com/apache/grails-redis/releases/tag/v${redisVersion}", "Grails Redis ${redisVersion} Plugin Release Notes")
                            }
                            li {
                                a(href: "https://github.com/apache/grails-quartz/releases/tag/v${quartzVersion}", "Grails Quartz ${quartzVersion} Plugin Release Notes")
                            }
                            li {
                                a(href: "https://github.com/apache/grails-github-actions/releases/tag/v${grailsGithubActionsVersion}", "Grails GitHub Actions ${grailsGithubActionsVersion} Release Notes")
                            }
                            li {
                                a(href: "https://github.com/apache/grails-gradle-publish/releases/tag/v${grailsGradlePublishVersion}", "Grails Publish Gradle Plugin ${grailsGradlePublishVersion} Release Notes")
                            }
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

                    p 'NOTE: The following versions are not ASF releases. Links to these releases are provided here as a convenience.'
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

                    h3(class: 'columnheader', style: 'margin-bottom: 10px;', 'Grails Application Forge')

                    mkp.yieldUnescaped'The quickest way to get started with our application generator:'
                    p {
                        a(href: 'https://start.grails.org', 'Grails Application Forge')
                    }


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

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
package website.gradle.tasks

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.gradle.api.Project
import org.jsoup.Jsoup
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir
import website.gradle.GrailsWebsiteExtension

class ExportAlgoliaIndexTaskSpec extends Specification {

    @TempDir
    File tempDir

    def 'exports canonical guide sections and excludes redirect pages'() {
        given: 'a guide with a canonical index.html and a redirect index.html'
            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            def distDir = project.layout.buildDirectory.dir('dist/guides/demo/8/guide').get().asFile.tap { it.mkdirs() }
            new File(distDir, 'index.html').text = '''
                <html><head><title>Demo Guide</title>
                <link rel="canonical" href="https://grails.apache.org/guides/demo/8/guide/index.html" /></head>
                <body><nav>Navigation</nav><main id="main"><h1>Demo Guide</h1>
                <h2 id="install">Installation</h2><p>Install the application.</p>
                <h2 id="run">Run the application</h2><p>Run it locally.</p></main></body></html>
            '''
            def redirectFile = project.layout.buildDirectory.file('dist/guides/demo/8/index.html').get().asFile
            redirectFile.text = '<html><head><meta name="robots" content="noindex"><meta http-equiv="refresh" content="0; url=guide/index.html"></head></html>'
            project.layout.buildDirectory.dir('generated/algolia').get().asFile.mkdirs()
            project.layout.buildDirectory.file('generated/algolia/documentation-records.json').get().asFile.text = '[]'
            def extension = extension(project)
            new File(project.projectDir, 'conf/guides.yml').text = '''
                guides:
                  - name: demo
                    title: Demo Guide
                    versions:
                      '8':
                        tags: [demo]
            '''.stripIndent(16)
            ExportAlgoliaIndexTask.register(project, extension)

        when: 'the export task is executed'
            def task = project.tasks.getByName(ExportAlgoliaIndexTask.NAME) as ExportAlgoliaIndexTask
            task.export()
            def records = new JsonSlurper().parse(task.outputFile.get().asFile) as List<Map>

        then: 'the exported records include the canonical guide sections and exclude the redirect page'
            records.size() == 2
            records*.source.every { it == 'guides' }
            records*.guideTitle == ['Demo Guide', 'Demo Guide']
            records*.url == [
                'https://grails.apache.org/guides/demo/8/guide/index.html#install',
                'https://grails.apache.org/guides/demo/8/guide/index.html#run'
            ]
            records*.content.every { it.contains('Navigation') == false }
    }

    def 'exports FAQ questions as anchored website records'() {
        given: 'a FAQ page with questions and answers'
            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            def distDir = project.layout.buildDirectory.dir('dist').get().asFile.tap { it.mkdirs() }
            new File(distDir, 'faq.html').text = '''
                <html><head><title>Questions</title></head><body>
                <article><div class="question" id="question-one"><h2>How does it work?</h2><p>It works well.</p></div></article>
                </body></html>
            '''
            project.layout.buildDirectory.dir('generated/algolia').get().asFile.mkdirs()
            project.layout.buildDirectory.file('generated/algolia/documentation-records.json').get().asFile.text = '[]'
            ExportAlgoliaIndexTask.register(project, extension(project))

        when: 'the export task is executed'
            def task = project.tasks.getByName(ExportAlgoliaIndexTask.NAME) as ExportAlgoliaIndexTask
            task.export()
            def records = new JsonSlurper().parse(task.outputFile.get().asFile) as List<Map>

        then: 'the exported records include the FAQ question as a website record with an anchor URL'
            records.size() == 1
            records[0].source == 'website'
            records[0].contentType == 'faq'
            records[0].url == 'https://grails.apache.org/faq.html#question-one'
            records[0].title == 'How does it work?'
    }

    def 'exports individual plugin records instead of the generic plugin page title'() {
        given: 'a plugin list page with multiple plugins, including duplicates and missing URLs'
            def document = Jsoup.parse('''
                <html><body>
                <h1>Grails Plugins</h1>
                <ul class="plugin-list">
                    <li class="plugin"><div class="plugin-header"><h3 class="name"><a href="https://github.com/example/grails-csrf">Grails CSRF</a></h3></div>
                        <p class="desc">Protect applications from cross-site request forgery.</p>
                        <span class="grails-compat">6.0.0</span><ul class="labels"><li class="label"><a>#security</a></li></ul>
                    </li>
                    <li class="plugin"><div class="plugin-header"><h3 class="name"><a href="https://github.com/example/grails-csrf">Grails CSRF</a></h3></div>
                        <p class="desc">Duplicate tab entry.</p>
                    </li>
                    <li class="plugin"><div class="plugin-header"><h3 class="name">Grails Redis</h3></div>
                        <p class="desc">Redis integration.</p>
                    </li>
                </ul>
                </body></html>
            ''', 'https://grails.apache.org/plugins.html')

        when: 'the plugin export task is executed'
            def records = ExportAlgoliaIndexTask.recordsFromLocalDocument(
                    document,
                    'plugins.html',
                    'https://grails.apache.org/plugins.html'
            )

        then: 'the exported records include individual plugin entries with correct metadata and exclude duplicates or missing URLs'
            records*.title == ['Grails CSRF', 'Grails Redis']
            records*.contentType.every { it == 'plugin' }
            records*.url == [
                    'https://github.com/example/grails-csrf',
                    'https://grails.apache.org/plugins.html#grails-redis'
            ]
            records[0].hierarchy == [lvl0: 'Grails Plugins', lvl1: 'Grails CSRF']
            records[0].description == 'Protect applications from cross-site request forgery.'
            records[0].tags == ['security']
            records[0].grailsVersion == '6.0.0'
            ExportAlgoliaIndexTask.recordsFromLocalDocument(
                    document,
                    'plugins/tags/security.html',
                    'https://grails.apache.org/plugins/tags/security.html'
            ).isEmpty()
    }

    def 'external documentation records exclude noindex and preserve source metadata'() {
        given: 'a user documentation page with a configuration section'
            def document = Jsoup.parse('''
                <html><head><title>User Documentation</title></head><body>
                <main id="main"><h1>Configuration</h1><p>Configure Grails.</p></main>
                </body></html>
            ''', 'https://grails.apache.org/docs/8.0.0/guide/index.html')

        when: 'the export task is executed'
            def records = ExportAlgoliaIndexTask.recordsFromExternalDocument(
                    document,
                    'https://grails.apache.org/docs/8.0.0/guide/index.html',
                    'user-documentation',
                    '8.0.0'
            )

        then: 'the exported records include the configuration section with correct metadata and exclude any noindex pages'
            records.size() == 1
            records[0].source == 'user-documentation'
            records[0].contentType == 'user-documentation'
            records[0].grailsVersion == '8.0.0'
            records[0].url.endsWith('#configuration')
            records[0].description == ''
            records[0].tags == []
            records[0].hierarchy == [lvl0: 'Configuration', lvl1: 'Configuration']
            records[0].versionRank == 0
            records[0].sortKey == records[0].objectID
    }

    def 'external documentation records retain chapter sections as separate results'() {
        given: 'a user documentation page with multiple chapters and sections'
        def document = Jsoup.parse('''
            <html><body>
            <div id="navigation"><h2>Table of Contents</h2></div>
            <table id="colset"><tr><td><div id="main">
                    <h1>8 The Web Layer</h1>
                    <div class="contribute-btn"><button>Improve this doc</button></div>
                    <h2 id="controllers">8.1 Controllers</h2>
                    <div class="contribute-btn"><button>Improve this doc</button></div>
                    <div class="paragraph"><p>A controller handles requests.</p></div>
                    <h3 id="binding">8.1.1 Data Binding</h3>
                    <div class="paragraph"><p>Data binding maps request data.</p></div>
                    <h2 id="views">8.2 Views</h2>
                    <div class="paragraph"><p>A view renders responses.</p></div>
            </div></td></tr></table>
            </body></html>
        ''', 'https://grails.apache.org/docs/7.2.3/guide/theWebLayer.html')

        when: 'the export task is executed'
            def records = ExportAlgoliaIndexTask.recordsFromExternalDocument(
                    document,
                    'https://grails.apache.org/docs/7.2.3/guide/theWebLayer.html',
                    'user-documentation',
                    '7.2.3'
            )

        then: 'the exported records include each chapter and section as separate results with correct metadata'
            records*.title == ['Controllers', 'Data Binding', 'Views']
            records*.url == [
                    'https://grails.apache.org/docs/7.2.3/guide/theWebLayer.html#controllers',
                    'https://grails.apache.org/docs/7.2.3/guide/theWebLayer.html#binding',
                    'https://grails.apache.org/docs/7.2.3/guide/theWebLayer.html#views'
            ]
            records[0].hierarchy == [lvl0: 'The Web Layer', lvl1: 'Controllers']
            records[1].hierarchy == [lvl0: 'The Web Layer', lvl1: 'Controllers', lvl2: 'Data Binding']
    }

    def 'exports blog posts with metadata and excludes the blog archive'() {
        given: 'a blog post with a title, description, and date, and a blog archive page'
            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            def distDir = project.layout.buildDirectory.dir('dist/blog').get().asFile.tap { it.mkdirs() }
            new File(distDir, '2024-01-01-release.html').text = '''
                <html><head><title>Release post</title>
                <meta name="description" content="A release announcement." />
                <meta name="date" content="January 1, 2024" /></head>
                <body><article class="post"><h1>Release post</h1><p>Grails release news.</p></article></body></html>
            '''
            new File(distDir, 'index.html').text =
                    '<html><head><title>Blog</title></head><body><article class="post"><h1>Blog</h1></article></body></html>'
            def tagFile = new File(distDir, 'tag/grails.html').tap { it.parentFile.mkdirs() }
            tagFile.text =
                    '<html><head><title>Grails</title></head><body><article class="post"><h1>Grails</h1></article></body></html>'
            prepareExport(project)

        when: 'the export task is executed'
            def task = project.tasks.getByName(ExportAlgoliaIndexTask.NAME) as ExportAlgoliaIndexTask
            task.export()
            def records = new JsonSlurper().parse(task.outputFile.get().asFile) as List<Map>

        then: 'the exported records include the blog post with correct metadata and exclude the blog archive and tag pages'
            records.size() == 1
            records[0].contentType == 'blog'
            records[0].url == 'https://grails.apache.org/blog/2024-01-01-release.html'
            records[0].description == 'A release announcement.'
            records[0].date == 'January 1, 2024'
            records[0].hierarchy == [lvl0: 'Release post']
    }

    def 'exports API records with API source and stable schema fields'() {
        given: 'an API page with a title, description, and content'
            def document = Jsoup.parse('''
                <html><head><title>Book (Grails API)</title>
                <meta name="description" content="Book API reference." /></head>
                <body><main><h1>Book</h1><p>Represents a book.</p><pre>String title</pre></main></body></html>
            ''', 'https://grails.apache.org/docs/8.0.0/api/example/Book.html')

        when: 'the export task is executed'
            def records = ExportAlgoliaIndexTask.recordsFromExternalDocument(
                    document, 'https://grails.apache.org/docs/8.0.0/api/example/Book.html',
                    'api', '8.0.0')

        then: 'the exported records include the API page with correct metadata and stable schema fields'
        records.size() == 1
        records[0].source == 'api'
        records[0].contentType == 'api'
        records[0].grailsVersion == '8.0.0'
        records[0].description == 'Book API reference.'
        records[0].sourceRank == 0
        records[0].content.contains('String title')
        records[0].tags == []
        records[0].hierarchy == [lvl0: 'Book (Grails API)']
        records[0].versionRank == 0
        records[0].sortKey == records[0].objectID
    }

    def 'uses stable IDs, deterministic order, and removes duplicate URLs'() {
        given: 'multiple HTML files with different titles and content, including a duplicate URL'
            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            def distDir = project.layout.buildDirectory.dir('dist').get().asFile.tap { it.mkdirs() }
            new File(distDir, 'z.html').text = '<html><head><title>Zed</title></head><body><article class="post"><p>Z content</p></article></body></html>'
            new File(distDir, 'a.html').text = '<html><head><title>Aye</title></head><body><article class="post"><p>A content</p></article></body></html>'
            new File(distDir, 'duplicate.html').text = '<html><head><title>Duplicate</title><link rel="canonical" href="https://grails.apache.org/a.html" /></head><body><article class="post"><p>Duplicate content</p></article></body></html>'
            prepareExport(project)

        when: 'the export task is executed twice to verify stable IDs and deterministic order'
            def task = project.tasks.getByName(ExportAlgoliaIndexTask.NAME) as ExportAlgoliaIndexTask
            task.export()
            def first = new JsonSlurper().parse(task.outputFile.get().asFile) as List<Map>
            task.export()
            def second = new JsonSlurper().parse(task.outputFile.get().asFile) as List<Map>

        then: 'the exported records have stable IDs, deterministic order, and no duplicate URLs'
            first*.url.toSet() == [
                    'https://grails.apache.org/a.html',
                    'https://grails.apache.org/z.html'
            ] as Set
            first*.objectID == second*.objectID
            first*.objectID == first*.objectID.sort()
            first.every { record ->
                record.keySet().containsAll([
                        'objectID', 'title', 'content', 'url', 'source', 'contentType',
                        'description', 'grailsVersion', 'tags', 'date', 'hierarchy',
                        'sourceRank', 'versionRank', 'sortKey'
                ])
            }
            first.every { it.sourceRank == 1 && it.versionRank == 0 && it.sortKey == it.objectID }
    }

    def 'writes an empty export when there is no local or external content'() {
        given: 'a project with no local or external content'
            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            prepareExport(project)

        when: 'the export task is executed'
            def task = project.tasks.getByName(ExportAlgoliaIndexTask.NAME) as ExportAlgoliaIndexTask
            task.export()

        then: 'the exported records file is empty'
            new JsonSlurper().parse(task.outputFile.get().asFile) == []
    }

    def 'ranks newer documentation versions ahead of older versions'() {
        given: 'a project with documentation records for different versions'
            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            project.layout.buildDirectory.dir('generated/algolia').get().asFile.mkdirs()
            project.layout.buildDirectory.file('generated/algolia/documentation-records.json').get().asFile.text = JsonOutput.toJson([
                    ExportAlgoliaIndexTask.recordsFromExternalDocument(
                            Jsoup.parse('<html><head><title>Older</title></head><body><main><h1>Older</h1><p>Old content.</p></main></body></html>'),
                            'https://grails.apache.org/docs/7.0.0/guide/index.html',
                            'user-documentation', '7.0.0')[0],
                    ExportAlgoliaIndexTask.recordsFromExternalDocument(
                            Jsoup.parse('<html><head><title>Newer</title></head><body><main><h1>Newer</h1><p>New content.</p></main></body></html>'),
                            'https://grails.apache.org/docs/8.0.0/guide/index.html',
                            'user-documentation', '8.0.0')[0]
            ])
            def extension = extension(project)
            new File(project.projectDir, 'conf/releases.yml').text = '''
                coreReleases:
                  - version: '7.0.0'
                  - version: '8.0.0'
            '''.stripIndent(16)
            ExportAlgoliaIndexTask.register(project, extension)

        when: 'the export task is executed'
            def task = project.tasks.getByName(ExportAlgoliaIndexTask.NAME) as ExportAlgoliaIndexTask
            task.export()
            def records = new JsonSlurper().parse(task.outputFile.get().asFile) as List<Map>

        then: 'the exported records include both versions and rank the newer version ahead of the older version'
            records*.grailsVersion.toSet() == ['7.0.0', '8.0.0'] as Set
            records.find { it.grailsVersion == '8.0.0' }.versionRank >
                    records.find { it.grailsVersion == '7.0.0' }.versionRank
    }

    def 'redirect and frameset documents are excluded'() {
        given: 'a redirect page and a frameset page'
            def redirect = Jsoup.parse('''
                <html><head><meta http-equiv="refresh" content="0; url=other.html"></head><body></body></html>
            ''')
            def frameset = Jsoup.parse('<html><frameset><frame src="guide/index.html"></frameset></html>')

        expect: 'the export task excludes redirect and frameset documents'
            ExportAlgoliaIndexTask.recordsFromExternalDocument(
                    redirect, 'https://grails.apache.org/docs/8.0.0/guide/old.html',
                    'user-documentation', '8.0.0').isEmpty()
            ExportAlgoliaIndexTask.recordsFromExternalDocument(
                    frameset, 'https://grails.apache.org/docs/8.0.0/api/index.html',
                    'api', '8.0.0').isEmpty()
            ExportAlgoliaIndexTask.recordsFromExternalDocument(
                    Jsoup.parse('<html><body>Overview</body></html>'),
                    'https://grails.apache.org/docs/8.0.0/api/overview-summary.html',
                    'api', '8.0.0').isEmpty()
    }

    private static GrailsWebsiteExtension extension(Project project) {
        def extension = project.extensions.create('grailsWebsite', GrailsWebsiteExtension)
        extension.url.set('https://grails.apache.org')
        extension.outputDir.set(project.layout.buildDirectory)
        def guidesYmlFile = new File(project.projectDir, 'conf/guides.yml')
        guidesYmlFile.parentFile.mkdirs()
        guidesYmlFile.text = 'guides: []\n'
        new File(project.projectDir, 'conf/releases.yml').text = 'coreReleases: []\n'
        extension
    }

    private static void prepareExport(Project project) {
        project.layout.buildDirectory.dir('generated/algolia').get().asFile.mkdirs()
        project.layout.buildDirectory.file('generated/algolia/documentation-records.json').get().asFile.text = '[]'
        ExportAlgoliaIndexTask.register(project, extension(project))
    }
}

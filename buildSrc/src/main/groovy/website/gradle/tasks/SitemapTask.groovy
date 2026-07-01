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
package website.gradle.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

import website.gradle.GrailsWebsiteExtension

import static groovy.io.FileType.FILES

@CompileStatic
@CacheableTask
abstract class SitemapTask extends GrailsWebsiteTask {

    @Internal
    final String description = 'Generates build/dist/sitemap.xml with every page in the site'

    public static final String NAME = 'genSitemap'

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getInputDir()

    @Input
    abstract Property<String> getUrl()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    static TaskProvider<SitemapTask> register(
            Project project,
            GrailsWebsiteExtension siteExt,
            String name = NAME
    ) {
        project.tasks.register(name, SitemapTask) {
            it.inputDir.set(siteExt.outputDir.dir('dist'))
            it.url.set(siteExt.url)
            it.outputFile.set(siteExt.outputDir.file('dist/sitemap.xml'))
        }
    }

    // A page that redirects elsewhere (meta-refresh) or opts out of indexing
    // (robots noindex) must never appear in the sitemap.
    private static final Pattern REFRESH = Pattern.compile(
            '(?is)<meta[^>]+http-equiv\\s*=\\s*["\']refresh["\']')
    private static final Pattern NOINDEX = Pattern.compile(
            '(?is)<meta[^>]+name\\s*=\\s*["\']robots["\'][^>]*noindex')
    // <link rel="canonical" href="..."> - if a page declares a canonical that
    // is not itself, it is a duplicate and the canonical owns the sitemap slot.
    private static final Pattern CANONICAL = Pattern.compile(
            '(?is)<link[^>]+rel\\s*=\\s*["\']canonical["\'][^>]*href\\s*=\\s*["\']([^"\']+)["\']')

    @TaskAction
    void renderSitemap() {
        String websiteUrl = url.get()
        List<String> urls = []
        File inputDirectory = inputDir.get().asFile
        String rootPath = inputDirectory.absolutePath
        inputDirectory.eachFileRecurse(FILES) { File f ->
            if (!f.name.endsWith('.html')) {
                return
            }
            String relPath = f.absolutePath.substring(rootPath.length()).replace('\\', '/')
            String ownUrl = websiteUrl + relPath
            if (isIndexable(f, ownUrl)) {
                urls.add(ownUrl)
            }
        }
        outputFile.get().asFile.with {
            parentFile.mkdirs()
            text = sitemapContent(urls.sort())
        }
    }

    /**
     * A page belongs in the sitemap only if it is not a redirect / noindex stub
     * and either declares no canonical or names itself as the canonical. This
     * drops the per-guide duplicates (the per-version root {@code index.html},
     * {@code guide/single.html}, {@code guide/pages/*.html} redirects and the
     * per-chapter pages that point their canonical at {@code guide/index.html})
     * while leaving every ordinary site page untouched.
     */
    private static boolean isIndexable(File f, String ownUrl) {
        String text = f.getText('UTF-8')
        if (REFRESH.matcher(text).find() || NOINDEX.matcher(text).find()) {
            return false
        }
        Matcher m = CANONICAL.matcher(text)
        if (m.find()) {
            return pathOnly(m.group(1)) == pathOnly(ownUrl)
        }
        return true
    }

    /** Strips scheme + host so canonical/own URLs compare by path alone. */
    private static String pathOnly(String u) {
        Matcher m = Pattern.compile('^[a-zA-Z][a-zA-Z0-9+.-]*://[^/]+(/.*)?$').matcher(u)
        if (m.matches()) {
            return m.group(1) ?: '/'
        }
        return u
    }

    @CompileDynamic
    static String sitemapContent(List<String> urls) {
        def writer = new StringWriter()
        def printer = new IndentPrinter(new PrintWriter(writer), '', false)
        def xml = new MarkupBuilder(printer)
        xml.urlset(xmlns: 'https://www.sitemaps.org/schemas/sitemap/0.9') {
            for (def urlStr : urls) {
                url {
                    loc(urlStr)
                }
            }
        }
        '<?xml version="1.0" encoding="UTF-8"?>\n' + writer.toString()
    }
}

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

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import groovy.json.JsonSlurper

import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir
import website.gradle.GrailsWebsiteExtension

class CollectAlgoliaDocumentationTaskSpec extends Specification {

    @TempDir
    File tempDir

    HttpServer server

    def cleanup() {
        server?.stop(0)
    }

    def 'skips unavailable documentation versions without discarding available versions'() {
        given: 'a mock HTTP server that serves only the 7.2.3 documentation and fails for 0.6'
            server = HttpServer.create(new InetSocketAddress('localhost', 0), 0)
            server.createContext('/docs/0.6/') { HttpExchange exchange ->
                throw new AssertionError('Grails 0.6 must not be requested' as Object)
            }
            server.createContext('/docs/7.2.3/guide/') { HttpExchange exchange ->
                respond(exchange, 200, '''
                    <html><head><title>Grails Guide</title></head><body>
                    <main><h1>Getting Started</h1><p>Build a Grails application.</p></main>
                    </body></html>
                ''')
            }
            server.createContext('/docs/7.2.3/api/') { HttpExchange exchange ->
                respond(exchange, 200, '<html><body><a href="Book.html">Book</a></body></html>')
            }
            server.createContext('/docs/7.2.3/api/Book.html') { HttpExchange exchange ->
                respond(exchange, 200, '''
                    <html><head><title>Book API</title></head><body>
                    <main><h1>Book</h1><p>Book API.</p></main>
                    </body></html>
                ''')
            }
            server.start()

            def project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            def releasesYmlFile = new File(tempDir, 'conf/releases.yml').tap { it.parentFile.mkdirs() }
            releasesYmlFile.text = "coreReleases:\n  - version: '7.2.3'\n"
            def extension = project.extensions.create('grailsWebsite', GrailsWebsiteExtension)
            extension.releases.set(releasesYmlFile)
            def task = CollectAlgoliaDocumentationTask.register(project, extension).get()
            task.documentationBaseUrl.set("http://localhost:${server.address.port}/docs")
            task.versionSelection.set('0.6,7.2.3')
            task.collectExternal.set(true)

        when: 'the task is executed'
            task.collect()
            def records = new JsonSlurper().parse(task.outputFile.get().asFile) as List<Map>

        then: 'the task collects records only for the available 7.2.3 version'
            records.size() == 3
            records*.grailsVersion.toSet() == ['7.2.3'] as Set
            records*.source.toSet() == ['user-documentation', 'api'] as Set
    }

    private static void respond(HttpExchange exchange, int status, String body) {
        def bytes = body.getBytes('UTF-8')
        exchange.sendResponseHeaders(status, bytes.length)
        exchange.responseBody.withCloseable { it.write(bytes) }
    }
}

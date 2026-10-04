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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

@CompileStatic
@DisableCachingByDefault(because = 'Uploads data to an external Algolia service')
abstract class UploadAlgoliaIndexTask extends GrailsWebsiteTask {

    static final String NAME = 'uploadAlgoliaIndex'
    static final String INDEX_NAME = GenerateAlgoliaConfigTask.INDEX_NAME

    @Internal
    final String description =
            'Uploads the generated records to Algolia'

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getRecordsFile()

    @Internal
    abstract Property<String> getApplicationId()

    @Internal
    abstract Property<String> getWriteApiKey()

    static TaskProvider<UploadAlgoliaIndexTask> register(Project project, String name = NAME) {
        project.tasks.register(name, UploadAlgoliaIndexTask) {
            it.recordsFile.set(
                    project.layout.buildDirectory
                            .file('generated/algolia/records.json')
            )
            it.applicationId.set(
                    project.providers
                            .environmentVariable('ALGOLIA_APP_ID')
                            .orElse('')
            )
            it.writeApiKey.set(
                    project.providers
                            .environmentVariable('ALGOLIA_WRITE_API_KEY')
                            .orElse('')
            )
        }
    }

    @TaskAction
    void upload() {
        def appId = applicationId.get().trim()
        def writeKey = writeApiKey.get().trim()
        if (!appId) throw new IllegalStateException('ALGOLIA_APP_ID is required for uploadAlgoliaIndex.')
        if (!writeKey) throw new IllegalStateException('ALGOLIA_WRITE_API_KEY is required for uploadAlgoliaIndex.')

        def stagingIndex = "${INDEX_NAME}_staging"
        deleteIndex(appId, writeKey, stagingIndex)
        int recordCount = batchRecords(appId, writeKey, stagingIndex, recordsFile.get().asFile)
        if (recordCount == 0) {
            throw new IllegalStateException('The Algolia export contains no records; refusing to replace the live index.')
        }
        updateSettings(appId, writeKey, stagingIndex)
        moveIndex(appId, writeKey, stagingIndex, INDEX_NAME)
        logger.info(
                'Uploaded {} Algolia record(s) to {}.',
                recordCount, INDEX_NAME
        )
    }

    private static int batchRecords(String appId, String key, String index, File recordsFile) {
        int batchSize = 500
        def requests = new ArrayList<Map<String, Object>>(batchSize)
        int[] count = [0]
        streamJsonArray(recordsFile) { Map<String, Object> record ->
            requests << [action: 'addObject', body: record]
            if (requests.size() == batchSize) {
                submitAndWait(appId, key, index, 'POST', "/1/indexes/${encode(index)}/batch", [requests: requests])
                count[0] += requests.size()
                requests.clear()
            }
        }
        if (!requests.isEmpty()) {
            submitAndWait(appId, key, index, 'POST', "/1/indexes/${encode(index)}/batch", [requests: requests])
            count[0] += requests.size()
        }
        count[0]
    }

    private static void streamJsonArray(File input, Closure consumer) {
        input.withReader('UTF-8') { Reader reader ->
            StringBuilder object = null
            int depth = 0
            boolean quoted = false
            boolean escaped = false
            int next
            while ((next = reader.read()) != -1) {
                char character = (char) next
                if (object == null) {
                    if (character == '{' as char) {
                        object = new StringBuilder()
                        object.append(character)
                        depth = 1
                        quoted = false
                        escaped = false
                    }
                    continue
                }
                object.append(character)
                if (escaped) {
                    escaped = false
                    continue
                }
                if (quoted && character == '\\' as char) {
                    escaped = true
                    continue
                }
                if (character == '"' as char) {
                    quoted = !quoted
                    continue
                }
                if (quoted) continue
                if (character == '{' as char) depth++
                if (character == '}' as char) depth--
                if (depth == 0) {
                    Object parsed = new JsonSlurper().parseText(object.toString())
                    if (parsed instanceof Map) consumer.call(parsed as Map<String, Object>)
                    object = null
                }
            }
            if (object != null) throw new IllegalArgumentException("Malformed JSON record array: ${input}")
        }
    }

    private static void updateSettings(String appId, String key, String index) {
        submitAndWait(appId, key, index, 'PUT', "/1/indexes/${encode(index)}/settings", [
                searchableAttributes: ['title', 'hierarchy', 'content', 'description', 'tags'],
                attributesForFaceting: ['source', 'contentType', 'grailsVersion', 'tags'],
                attributesToHighlight: ['title', 'content'],
                attributesToSnippet: ['content:30'],
                customRanking: ['desc(sourceRank)', 'desc(versionRank)', 'asc(sortKey)']
        ])
    }

    private static void deleteIndex(String appId, String key, String index) {
        try {
            submitAndWait(appId, key, index, 'DELETE', "/1/indexes/${encode(index)}", null)
        } catch (IllegalStateException ex) {
            if (!ex.message.contains('404')) throw ex
        }
    }

    private static void moveIndex(String appId, String key, String source, String destination) {
        submitAndWait(appId, key, source, 'POST', "/1/indexes/${encode(source)}/operation", [
                operation: 'move',
                destination: destination
        ])
    }

    private static void submitAndWait(
            String appId,
            String key,
            String index,
            String method,
            String path,
            Object body
    ) {
        def response = request(appId, key, method, path, body)
        if (!(response instanceof Map) || !(response as Map).taskID) return
        def taskId = String.valueOf((response as Map).taskID)
        long deadline = System.currentTimeMillis() + 120000
        while (System.currentTimeMillis() < deadline) {
            def task = request(
                    appId,
                    key,
                    'GET',
                    "/1/indexes/${encode(index)}/task/${encode(taskId)}",
                    null
            )
            if (task instanceof Map && (task as Map).status == 'published') return
            Thread.sleep(500)
        }
        throw new IllegalStateException("Algolia task ${taskId} did not complete within 120 seconds.")
    }

    private static Object request(String appId, String key, String method, String path, Object body) {
        HttpURLConnection connection = null
        try {
            connection = (HttpURLConnection) new URI("https://${appId}.algolia.net${path}").toURL().openConnection()
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 60000
            connection.doInput = true
            connection.setRequestProperty('X-Algolia-Application-Id', appId)
            connection.setRequestProperty('X-Algolia-API-Key', key)
            connection.setRequestProperty('Content-Type', 'application/json')
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.withWriter('UTF-8') { it << JsonOutput.toJson(body) }
            }
            int status = connection.responseCode
            String response = (status >= 200 && status < 300 ? connection.inputStream : connection.errorStream)?.getText('UTF-8') ?: ''
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Algolia request failed with HTTP ${status}: ${response}")
            }
            response ? new JsonSlurper().parseText(response) : null
        } finally {
            connection?.disconnect()
        }
    }

    private static String encode(String value) {
        URLEncoder.encode(value, 'UTF-8').replace('+', '%20')
    }
}

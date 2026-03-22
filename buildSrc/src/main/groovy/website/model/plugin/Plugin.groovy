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
package website.model.plugin

import java.time.LocalDateTime

import groovy.transform.CompileStatic

/**
 * Represents a Grails plugin with metadata from the plugins index.
 */
@CompileStatic
class Plugin {

    /** Plugin display name */
    String name

    /** Plugin description */
    String desc

    /** Latest published version (e.g., "2.0.0") */
    String latestVersion

    /** Version control URL (GitHub, etc.) */
    String vcsUrl

    /** Documentation URL */
    String docsUrl

    /** Maven coordinates (e.g., "io.github.matrei:grails-csrf") */
    String coords

    /** Maven repository URL */
    String mavenRepo

    /** License identifier (e.g., "Apache-2.0") */
    String license

    /** Deprecation message if the plugin is deprecated, null otherwise */
    String deprecated

    /** Minimum Grails version required (e.g., "6.0.0 > *") */
    String grailsVersion

    /** GitHub star count, if available */
    Integer githubStars

    /** Plugin owner/maintainer */
    Owner owner

    /** Tags/labels for categorization */
    List<String> labels = []

    /** All available versions of this plugin */
    List<PluginVersion> versions = []

    /** Date of last update */
    LocalDateTime updated
}

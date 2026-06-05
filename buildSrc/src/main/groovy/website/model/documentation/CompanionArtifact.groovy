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

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * An Apache-released companion artifact that ships alongside a particular
 * Grails major version with its own independent version number. Examples
 * include the Spring Security, Redis, Quartz, GitHub Actions, and Gradle
 * Publish plugins.
 *
 * <p>Loaded from the {@code companionArtifacts:} block of
 * {@code conf/releases.yml} via {@link SiteMap#companionArtifactsFor}. The
 * fields parallel the YAML schema documented in that file.
 */
@Immutable
@CompileStatic
class CompanionArtifact {

    /**
     * Maven artifact id and file-name slug used in the Apache mirror URL
     * (i.e. {@code apache-<artifactId>-<version>-bin.zip}).
     */
    String artifactId

    /** The plugin's own version, independent of grails-core. */
    String version

    /**
     * Sub-path under {@code https://www.apache.org/dyn/closer.lua/grails/}
     * where the artifact's distribution lives.
     */
    String mirrorDirectory

    /**
     * GitHub slug ({@code org/repo}) used for the {@code v<version>} release-notes
     * link. Kept as GitHub per the multi-version migration plan.
     */
    String releaseNotesRepo

    /** Human-readable label rendered on the downloads page. */
    String displayName
}

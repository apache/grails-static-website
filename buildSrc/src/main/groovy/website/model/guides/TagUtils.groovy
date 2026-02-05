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
package website.model.guides

import groovy.transform.CompileStatic

import website.model.plugin.Plugin
import website.model.tags.Tag

@CompileStatic
class TagUtils {

    static Set<Tag> populateTags(List<Guide> guides) {
        collectTags(guides?.collectMany { it.tags ?: [] })
    }

    static Set<Tag> populateTagsByPlugins(List<Plugin> plugins) {
        collectTags(plugins?.collectMany { it.labels ?: ([] as List<String>) })
    }

    static Set<Tag> populateTagsByPluginOwners(List<Plugin> plugins) {
        collectTags(plugins?.collect { it.owner?.name }?.findAll { it })
    }

    private static Set<Tag> collectTags(List<String> tags) {
        Map<String, Integer> tagsMap = tags
                ?.collect { it.trim().toLowerCase() }
                ?.countBy { it }
                ?: [:]

        tagsMap.collect { k, v ->
            new Tag(title: k, occurrence: v)
        } as Set<Tag>
    }
}
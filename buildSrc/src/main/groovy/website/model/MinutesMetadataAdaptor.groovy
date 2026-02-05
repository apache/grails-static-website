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
package website.model

import groovy.transform.CompileStatic

@CompileStatic
class MinutesMetadataAdaptor implements MinutesMetadata {

    Map<String, String> metadata

    MinutesMetadataAdaptor(Map<String, String> metadata) {
        this.metadata = metadata

    }

    @Override
    String get(String name) {
        return metadata[name]
    }

    @Override
    String getUrl() {
        get('url')
    }

    @Override
    String getTitle() {
        get('title')
    }

    @Override
    String getDate() {
        get('date')
    }

    @Override
    Map<String, String> toMap() {
        metadata
    }
}

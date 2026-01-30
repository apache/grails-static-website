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
package org.grails

import groovy.transform.CompileStatic

import org.grails.utils.DateUtils

@CompileStatic
class Page implements Content, Comparable<Page> {

    String filename
    String content
    Map<String, String> metadata

    @Override
    String getPath() {
        filename?.replaceAll('\\\\', '/')
    }

    @Override
    String getTitle() {
        metadata?.get('title')
    }

    @Override
    String getBody() {
        metadata?.get('body')
    }

    @Override
    String getDate() {
        metadata?.get('date')
    }

    @Override
    String getDescription() {
        metadata?.get('description')
    }

    @Override
    List<String> getKeywords() {
        metadata?.get('keywords')?.split(',') as List<String>
    }

    @Override
    String getRobots() {
        metadata?.get('robots')
    }

    @Override
    int compareTo(Page otherPage) {
        // Use reverse comparison for descending order (newest first)
        otherPage.parsedDate <=> parsedDate
    }

    Date getParsedDate() {
        DateUtils.parseDate(date)
    }
}

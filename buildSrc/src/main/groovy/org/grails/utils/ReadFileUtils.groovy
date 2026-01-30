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
package org.grails.utils

import groovy.transform.CompileStatic

@CompileStatic
class ReadFileUtils {

    static String readFileContent(ClassLoader classLoader, String filename) {
        def file = getFileFromURL(classLoader, filename)
        if (!file?.exists()) {
            file = new File("buildSrc/build/resources/main/$filename")
            if (!file.exists()) {
                return null
            }
        }
        file.text
    }

    static File getFileFromURL(ClassLoader classLoader, String filename) {
        def url = classLoader.getResource(filename)
        if (url == null) {
            return null
        }
        try {
            return new File(url.toURI())
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            return new File(url.getPath())
        }
    }
}

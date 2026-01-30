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

import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension

@CompileStatic
class MarkdownUtils {

    private final static DataHolder OPTIONS = new MutableDataSet().tap {
        it.set(Parser.EXTENSIONS, [TablesExtension.create()] as Collection<Extension>)
        it.toImmutable()
    }
    private final static Parser PARSER = Parser.builder(OPTIONS).build()
    private final static HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build()

    static String htmlFromMarkdown(String input) {
        RENDERER.render(
                PARSER.parse(input)
        )
    }
}

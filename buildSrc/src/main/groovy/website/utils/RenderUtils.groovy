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
package website.utils

import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

@CompileStatic
class RenderUtils {

    /** Environment variable to enable pretty (non-compact) HTML output */
    private static final String PRETTY_HTML_ENV_VAR = 'GRAILS_SITE_PRETTY_HTML'

    /**
     * Renders MarkupBuilder DSL into a String.
     *
     * By default, renders compact HTML (no indentation or newlines).
     * Set the GRAILS_SITE_PRETTY_HTML environment variable to any value
     * to enable pretty-printed HTML with indentation.
     *
     * Usage:
     *   def html = renderMarkup {
     *     div(class: 'box') {
     *       h1 'Hello'
     *     }
     *   }
     */
    static String renderHtml(
            @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MarkupBuilder) Closure<?> dsl
    ) {
        def writer = new StringWriter()
        def prettyPrint = System.getenv(PRETTY_HTML_ENV_VAR) != null
        def printer = new IndentPrinter(
                new PrintWriter(writer),
                prettyPrint ? '  ' : '',
                prettyPrint
        )
        def mb = new MarkupBuilder(printer)
        def c = (Closure) dsl.clone()
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.delegate = mb
        c.call()
        writer.toString()
    }
}

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
package website.model.questions

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import website.model.PageElement
import website.utils.MarkdownUtils

import static website.utils.RenderUtils.renderHtml

@CompileStatic
trait QuestionHtml implements PageElement {

    abstract String getTitle()
    abstract String getAnswer()
    abstract String getSlug()

    @Override
    @CompileDynamic
    String renderAsHtml() {
        renderHtml {
            div(class: 'question', id: slug) {
                h2 {
                    mkp.yieldUnescaped title
                }
                def text = MarkdownUtils.htmlFromMarkdown(answer)
                text = text.replaceAll('\\\\n', '<br/>')
                mkp.yieldUnescaped(text)
            }
        }
    }

}
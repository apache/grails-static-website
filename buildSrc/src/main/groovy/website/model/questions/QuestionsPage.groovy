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


import org.yaml.snakeyaml.Yaml
import website.utils.MarkdownUtils

import static website.utils.RenderUtils.renderHtml

class QuestionsPage {

    static String mainContent(File questions) {
        def model = new Yaml().load(questions.newDataInputStream())
        def questionList = model['questions'].collect {
            new Question(it as Map)
        }
        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    h1('Questions')
                }
            }
            div(class: 'content') {
                article(id: 'questions') {
                    for (Question question : questionList) {
                        div(class: 'question', id: question.slug) {
                            h2(class: 'column-header', question.title)
                            mkp.yieldUnescaped(
                                    MarkdownUtils.htmlFromMarkdown(question.answer)
                            )
                        }
                    }
                }
            }
        }
    }
}

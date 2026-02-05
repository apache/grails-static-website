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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import website.model.PageElement

import static website.utils.RenderUtils.renderHtml

@CompileStatic
trait GuideGroupHtml implements PageElement {

    abstract String getDescription()
    abstract String getImage()
    abstract String getUlId()
    abstract String getTitle()

    abstract List<GuideGroupItem> getItems()

    @Override
    String renderAsHtml() {
        renderAsHtmlWithStyleAttr(null)
    }

    @CompileDynamic
    String renderAsHtmlWithStyleAttr(String styleAttr) {
        renderHtml {
            omitNullAttributes = true
            div(class: 'guide-group', style: styleAttr) {
                div(class: 'guide-group-header') {
                    img(src: image, alt: title)
                    h2 {
                        mkp.yieldUnescaped(title)
                    }
                }
                ul(id: ulId) {
                    if (description) {
                        li(class: 'legend', description)
                    }
                    if (items) {
                        for (def item : items) {
                            if (item.href) {
                                li {
                                    if (item.image) {
                                        img(
                                                class: 'align-left',
                                                style: 'with: 70px; margin-right: 10px',
                                                src: item.image,
                                                alt: item.title
                                        )
                                    }
                                    a(href: item.href, item.title)
                                    if (item.legend) {
                                        p(item.legend)
                                    }
                                }
                            } else {
                                li(item.title)
                            }
                        }
                    }
                }
            }
        }
    }
}

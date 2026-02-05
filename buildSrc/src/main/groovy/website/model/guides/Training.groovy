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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import static website.utils.RenderUtils.renderHtml

@CompileStatic
class Training {

    @CompileDynamic
    static String training() {
        renderHtml {
            div(class: 'training') {
                h3(class: 'column-header') {
                    a(href: 'https://objectcomputing.com/training/catalog/grails/', 'Grails Training')
                }
                mkp.yieldUnescaped('[%events]')
            }
        }
    }
}

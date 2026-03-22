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

import org.yaml.snakeyaml.Yaml

import website.utils.MarkdownUtils

import static website.utils.RenderUtils.renderHtml

@CompileStatic
class ProfilesPage {

    @CompileDynamic
    static String mainContent(File profiles) {
        renderHtml {
            div(class: 'header-bar chalices-bg') {
                div(class: 'content') {
                    h1('Profiles')
                }
            }
            def categories = ProfilesPage.categories(profiles)
            div(class: 'content') {
                div(class: 'two-columns') {
                    div(class: 'odd column') {
                        mkp.yieldUnescaped(ProfilesPage.renderCategory(categories.find {
                            it.title == 'Profiles'
                        }))
                    }
                    div(class: 'column') {
                        mkp.yieldUnescaped(ProfilesPage.renderCategory(categories.find {
                            it.title == 'Plugin Profiles'
                        }))
                        mkp.yieldUnescaped(ProfilesPage.renderCategory(categories.find {
                            it.title == 'Third-Party Profiles'
                        }))

                    }
                }
            }
        }
    }

    /**
     * Reads a YAML "profiles" model file and groups the contained profiles into categories.
     * <p>
     * The YAML file is expected to contain a top-level {@code profiles} map. Each entry under
     * {@code profiles} represents one profile definition with at least {@code url} and {@code title},
     * and may also define category metadata such as {@code category}, {@code image}, and
     * {@code description}.
     * </p>
     * <p>
     * This method iterates over all profile definitions, creates (or reuses) a {@link ProfilesCategory}
     * for each distinct {@code category} value, copies category metadata (title/image/description)
     * from the YAML entry, and adds a {@link Profile} (url/title) to the category’s {@code profiles}
     * collection.
     * </p>
     *
     * @param modules the YAML file containing the profiles model (must be readable)
     * @return a collection of {@link ProfilesCategory} instances, each containing the profiles that
     *         belong to that category
     */
    private static Collection<ProfilesCategory> categories(File modules) {
        def model = modules.newInputStream().withCloseable { is ->
            new Yaml().load(is) as Map
        }
        Map<String, ProfilesCategory> byCategory = [:].withDefault { key ->
            new ProfilesCategory(title: key)
        }
        (model.profiles as Map).each { k, v ->
            def key = (v['category'] ?: 'Uncategorized') as String
            def cat = byCategory[key]
            cat.with {
                image = v['image'] ?: null
                description = v['description'] ?: null
            }
            cat.profiles << new Profile(url: v['url'], title: v['title'])
        }
        byCategory.values()
    }

    @CompileDynamic
    private static String renderCategory(ProfilesCategory cat) {
        if (!cat) return ''
        renderHtml {
            div(class: 'guide-group') {
                div(class: 'guide-group-header') {
                    img(src: cat.image, alt: cat.title)
                    h2(cat.title)
                }
                ul {
                    if (cat.description) {
                        def legend = MarkdownUtils.htmlFromMarkdown(cat.description)
                                .replaceAll('<p>', '')
                                .replaceAll('</p>', '')

                        li(class: 'legend') {
                            mkp.yieldUnescaped(legend)
                        }
                    }
                    cat.profiles.each { item ->
                        li { a(href: item.url, item.title) }
                    }
                }
            }
        }
    }

    static class ProfilesCategory {
        String title
        String image
        String description
        List<Profile> profiles = []
    }

    static class Profile {
        String title
        String url
    }
}

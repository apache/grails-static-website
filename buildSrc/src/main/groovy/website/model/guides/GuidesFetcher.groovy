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

import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import website.utils.DateUtils

@CompileStatic
class GuidesFetcher {

    /** URL to the remote JSON file containing guide metadata. */
    public static final String GUIDES_JSON =
            'https://raw.githubusercontent.com/grails-guides/grails-guides-template/gh-pages/guides.json'

    private static final String DEFAULT_BRANCH = 'master'

    /** Maps git branch names to Grails major version numbers. */
    private static final Map<String, Integer> BRANCH_TO_MAJOR = [
            grails3: 3,
            grails4: 4,
            master : 4,
            grails5: 5,
            grails6: 6,
    ]

    /**
     * Internal DTO for deserializing guide entries from JSON.
     * Mirrors the structure of the remote guides.json file.
     */
    private static final class GuideDto {
        String category
        String githubBranch
        String githubSlug
        String grailsVersion
        String name
        String publicationDate
        String subtitle
        String title

        List<String> authors
        List<String> tags
    }

    /**
     * Fetches and parses all guides from the remote JSON endpoint.
     * Groups guides by their GitHub slug and creates either a {@link SingleGuide}
     * or {@link GrailsVersionedGuide} depending on whether multiple branches exist.
     *
     * @param skipFuture if {@code true}, excludes guides with publication dates in the future
     * @return list of guides sorted by publication date in descending order (newest first)
     */
    static List<Guide> fetchGuides(boolean skipFuture = true) {
        def entries = parseGuides(GUIDES_JSON)
        def slugsToBranches = [:] as Map<String, Set<String>>
        entries.each { entry ->
            def slug = entry.githubSlug
            def branch = entry.githubBranch ?: DEFAULT_BRANCH
            slugsToBranches.computeIfAbsent(slug) { [] as Set<String> }.add(branch)
        }

        def guides = [] as List<Guide>
        for (def slug : slugsToBranches.keySet()) {
            def branches = slugsToBranches[slug]
            if (branches.size() == 1) {
                def branch = branches.first()
                def guideDto = entries.find {
                    it.githubSlug == slug && (!it.githubBranch || it.githubBranch == branch)
                }
                def guide = guideDto ? toSingleGuide(guideDto) : null
                if (guide) {
                    guides << guide
                }
            } else {
                def guide = toVersionedGuide(entries, slug, branches)
                if (guide) {
                    guides << guide
                }
            }
        }

        if (skipFuture) {
            guides = guides.findAll { it.publicationDate.before(tomorrow()) }
        }
        guides.sort { a, b ->
            b.publicationDate <=> a.publicationDate
        }
    }

    /**
     * Parses the remote JSON file and converts each entry into a {@link GuideDto}.
     *
     * @param url the URL of the guides JSON file
     * @return list of parsed guide DTOs
     */
    private static List<GuideDto> parseGuides(String url) {
        def json = new JsonSlurper().parseText(new URL(url).text)
        def entries = json as List<Map<String, Object>>
        entries.collect { entry ->
            new GuideDto(
                    grailsVersion: entry.grailsVersion,
                    authors: (entry['authors'] ?: []) as List<String>,
                    category: entry['category'],
                    githubSlug: entry['githubSlug'],
                    githubBranch: entry['githubBranch'],
                    name: entry['name'],
                    title: entry['title'],
                    subtitle: entry['subtitle'],
                    tags: (entry['tags'] ?: []) as List<String>,
                    publicationDate: entry['publicationDate']
            )
        } as List<GuideDto>
    }

    /**
     * Converts a {@link GuideDto} into a {@link SingleGuide} domain object.
     * Used when a guide exists only for a single Grails version/branch.
     *
     * @param dto the guide DTO to convert
     * @return a new {@link SingleGuide} instance with all fields populated
     */
    private static SingleGuide toSingleGuide(GuideDto dto) {
        def guide = new SingleGuide(
                versionNumber: dto.grailsVersion,
                authors: dto.authors,
                category: dto.category,
                githubSlug: dto.githubSlug,
                githubBranch: dto.githubBranch,
                name: dto.name,
                title: dto.title,
                subtitle: dto.subtitle,
                tags: dto.tags
        )
        setPublicationDate(guide, dto)
        guide
    }

    /**
     * Creates a {@link GrailsVersionedGuide} from multiple branch-specific DTOs.
     * Used when a guide exists across multiple Grails versions (e.g., grails3, grails4).
     * Aggregates tags from each branch and maps them to their respective major versions.
     *
     * @param entries all guide DTOs to search through
     * @param slug the GitHub slug identifying the guide repository
     * @param branches the set of branch names (e.g., grails3, grails4) for this guide
     * @return a new {@link GrailsVersionedGuide} or {@code null} if no matching DTOs found
     */
    private static GrailsVersionedGuide toVersionedGuide(
            List<GuideDto> entries,
            String slug,
            Set<String> branches
    ) {
        def guide = null
        for (def branch : branches) {
            def dto = entries.find {
                it.githubSlug == slug && it.githubBranch == branch
            }
            if (!dto) {
                continue
            }
            if (guide == null) {
                guide = new GrailsVersionedGuide()
            }
            guide.versionNumber = dto.grailsVersion
            guide.authors = dto.authors
            guide.category = dto.category
            guide.githubSlug = dto.githubSlug
            guide.githubBranch = dto.githubBranch
            guide.name = dto.name
            guide.title = dto.title
            guide.subtitle = dto.subtitle

            def majorVersion = BRANCH_TO_MAJOR[branch]
            if (majorVersion) {
                guide.grailsMayorVersionTags[majorVersion] = dto.tags
            }
            setPublicationDate(guide, dto)
        }
        guide
    }

    /**
     * Parses and sets the publication date on a guide from the DTO's date string.
     * Uses {@link DateUtils#parseDate} to handle the date parsing.
     *
     * @param guide the guide to update
     * @param dto the DTO containing the publication date string
     */
    private static void setPublicationDate(Guide guide, GuideDto dto) {
        if (dto.publicationDate) {
            guide.publicationDate = DateUtils.parseDate(dto.publicationDate)
        }
    }

    /**
     * Returns a {@link Date} representing tomorrow (current date plus one day).
     * Used for filtering out guides with future publication dates.
     *
     * @return tomorrow's date
     */
    @CompileDynamic
    static Date tomorrow() {
        use(TimeCategory) {
            new Date() + 1.day
        }
    }
}

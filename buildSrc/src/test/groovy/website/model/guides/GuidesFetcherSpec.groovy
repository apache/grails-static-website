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

import spock.lang.Specification
import spock.lang.TempDir

class GuidesFetcherSpec extends Specification {

    @TempDir
    File tempDir

    def 'fetches a single-version guide as a SingleGuide'() {
        given:
        File yml = writeYaml '''
guides:
  - name: 'demo'
    title: 'Demo Guide'
    subtitle: 'Demo subtitle'
    authors: ['Author']
    category: 'Web Layer'
    publicationDate: '2026-05-03'
    versions:
      '8':
        sourcePath: guides/demo/v8
        tags: ['htmx']
        sampleRef:
          repo: 'grails-guides/demo'
          branch: 'grails8'
'''
        when:
        def guides = GuidesFetcher.fetchGuides(yml)

        then:
        guides.size() == 1
        guides[0] instanceof SingleGuide
        guides[0].name == 'demo'
        guides[0].versionNumber == '8'
        guides[0].category == 'Web Layer'
        guides[0].tags == ['htmx']
        guides[0].githubBranch == 'grails8'
    }

    def 'fetches a multi-version guide as a GrailsVersionedGuide and uses the YAML version key as the major'() {
        given:
        File yml = writeYaml '''
guides:
  - name: 'multi'
    title: 'Multi Guide'
    authors: ['Author']
    category: 'Advanced Grails'
    publicationDate: '2026-05-03'
    versions:
      '6':
        sourcePath: guides/multi/v6
        tags: ['old']
        sampleRef:
          repo: 'grails-guides/multi'
          branch: 'master'
      '8':
        sourcePath: guides/multi/v8
        tags: ['new']
        sampleRef:
          repo: 'grails-guides/multi'
          branch: 'grails8'
'''
        when:
        def guides = GuidesFetcher.fetchGuides(yml)

        then:
        guides.size() == 1
        guides[0] instanceof GrailsVersionedGuide
        def versioned = guides[0] as GrailsVersionedGuide
        versioned.name == 'multi'
        // The YAML version key is preserved verbatim as the integer major version.
        // This is the regression-fix for the BRANCH_TO_MAJOR table that hardcoded
        // master->4 and was missing grails7/grails8.
        versioned.grailsMayorVersionTags.keySet() == [6, 8] as Set
        versioned.grailsMayorVersionTags[6] == ['old']
        versioned.grailsMayorVersionTags[8] == ['new']
    }

    def 'two YAML guides that share sampleRef.repo are still rendered as two separate guides'() {
        // Regression test for the slug-collision bug where the legacy
        // grails-as-docker-container guides shared a sampleRef.repo with the
        // new grails-docker-bootbuildimage guide and were silently merged
        // into one rendered card with broken /3/ and /4/ links.
        given:
        File yml = writeYaml '''
guides:
  - name: 'old-guide'
    title: 'Old Guide'
    authors: ['Author']
    category: 'Advanced Grails'
    publicationDate: '2018-01-15'
    versions:
      '3':
        sourcePath: guides/old-guide/v3
        tags: ['legacy']
        sampleRef:
          repo: 'grails-guides/shared-sample'
          branch: 'grails3'
  - name: 'new-guide'
    title: 'New Guide'
    authors: ['Author']
    category: 'Advanced Grails'
    publicationDate: '2026-05-03'
    versions:
      '8':
        sourcePath: guides/new-guide/v8
        tags: ['fresh']
        sampleRef:
          repo: 'grails-guides/shared-sample'
          branch: 'grails8'
'''
        when:
        def guides = GuidesFetcher.fetchGuides(yml)
        def names = guides*.name as Set

        then:
        guides.size() == 2
        names == ['old-guide', 'new-guide'] as Set
    }

    def 'two YAML versions that share the same branch are both kept when they belong to the same guide'() {
        // Regression test for grails-mock-basics: v3 and v4 both had branch=master
        // so the old (slug, branch)-set logic collapsed them to size==1 and
        // toSingleGuide silently returned only the first match (v3).
        given:
        File yml = writeYaml '''
guides:
  - name: 'mock-basics'
    title: 'Mock Basics'
    authors: ['Author']
    category: 'Grails Testing'
    publicationDate: '2017-04-24'
    versions:
      '3':
        sourcePath: guides/mock-basics/v3
        tags: ['v3']
        sampleRef:
          repo: 'grails-guides/mock-basics'
          branch: 'master'
      '4':
        sourcePath: guides/mock-basics/v4
        tags: ['v4']
        sampleRef:
          repo: 'grails-guides/mock-basics'
          branch: 'master'
'''
        when:
        def guides = GuidesFetcher.fetchGuides(yml)

        then:
        guides.size() == 1
        guides[0] instanceof GrailsVersionedGuide
        def versioned = guides[0] as GrailsVersionedGuide
        versioned.grailsMayorVersionTags.keySet() == [3, 4] as Set
        versioned.grailsMayorVersionTags[3] == ['v3']
        versioned.grailsMayorVersionTags[4] == ['v4']
    }

    def 'a future-dated guide is filtered out when skipFuture is true (the default)'() {
        given:
        File yml = writeYaml '''
guides:
  - name: 'future'
    title: 'Future Guide'
    authors: ['Author']
    category: 'Web Layer'
    publicationDate: '2099-01-01'
    versions:
      '8':
        sourcePath: guides/future/v8
        tags: []
        sampleRef:
          repo: 'grails-guides/future'
          branch: 'grails8'
'''
        when:
        def guides = GuidesFetcher.fetchGuides(yml)

        then:
        guides.isEmpty()
    }

    def 'a guide entry with no versions is silently skipped'() {
        given:
        File yml = writeYaml '''
guides:
  - name: 'empty'
    title: 'Empty'
    authors: ['Author']
    category: 'Advanced Grails'
    publicationDate: '2026-05-03'
    versions: {}
'''
        when:
        def guides = GuidesFetcher.fetchGuides(yml)

        then:
        guides.isEmpty()
    }

    def 'guides are sorted by publication date descending'() {
        given:
        File yml = writeYaml '''
guides:
  - name: 'older'
    title: 'Older'
    authors: ['Author']
    category: 'Advanced Grails'
    publicationDate: '2017-01-23'
    versions:
      '3':
        sourcePath: guides/older/v3
        tags: []
        sampleRef:
          repo: 'grails-guides/older'
          branch: 'grails3'
  - name: 'newer'
    title: 'Newer'
    authors: ['Author']
    category: 'Advanced Grails'
    publicationDate: '2026-05-03'
    versions:
      '8':
        sourcePath: guides/newer/v8
        tags: []
        sampleRef:
          repo: 'grails-guides/newer'
          branch: 'grails8'
'''
        when:
        def guides = GuidesFetcher.fetchGuides(yml)

        then:
        guides*.name == ['newer', 'older']
    }

    private File writeYaml(String content) {
        File f = new File(tempDir, 'guides.yml')
        f.text = content
        f
    }
}

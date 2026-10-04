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
package website.gradle.tasks

import spock.lang.Specification

class AlgoliaSearchPageSpec extends Specification {

    private static final File ROOT = new File(System.getProperty('user.dir')).name == 'buildSrc'
            ? new File(System.getProperty('user.dir')).parentFile
            : new File(System.getProperty('user.dir'))

    def 'search page exposes the accessible Algolia controls and states'() {
        given:
        def page = new File(ROOT, 'pages/search.html').getText('UTF-8')
        def header = new File(ROOT, 'templates/partials/site-header.html').getText('UTF-8')
        def template = new File(ROOT, 'templates/document.html').getText('UTF-8')
        def script = new File(ROOT, 'assets/javascripts/algolia-search.js').getText('UTF-8')
        def styles = new File(ROOT, 'assets/stylesheets/screen.css').getText('UTF-8')
        def logo = new File(ROOT, 'assets/images/algolia-logo-blue.svg')

        expect:
        with(page) {
            contains('Search the Apache Grails Website, Guides, User Documentation, and API Documentation.')
            contains("<p class='search-page-description'>")
            contains("<button type='button' class='hero-action hero-action-download search-page-open' data-search-open>")
            contains("<div class='search-page-action'>")
            contains('disableKapa: true')
            !toLowerCase().contains('global search')
            !toLowerCase().contains('google')

        }
        with(styles) {
            contains('.search-page-description {')
            contains('\ttext-align: center;')
        }
        with(header) {
            contains("data-search-open")
            contains("class='search-nav-icon'")
            contains("<kbd aria-label='Control or Command K'>Ctrl K</kbd>")
            contains("class='app-forge-nav-link'")
            !contains("class='app-forge-nav-icon'")
            contains("id='global-search-dialog'")
            contains("id='global-search-form'")
            contains("aria-label='Search'")
            !contains("id='global-search-title'")
            !contains("class='global-search-close'")
            !contains("<label for='global-search-input'>")
            contains("aria-label='Search the Grails website, guides and documentation'")
            contains("id='global-search-clear'")
            contains("aria-label='Clear search'")
            contains("class='global-search-clear'")
            contains("class='global-search-mobile-close' data-search-close>Close</button>")
            contains("<div class='global-search-input-control'>")
            contains("<option value='plugin'>Plugins</option>")
            contains("id='global-search-source-filter' aria-label='Collection'")
            contains("id='global-search-content-type-filter-container'")
            contains("id='global-search-content-type-filter' aria-label='Type'")
            contains("id='global-search-version-filter' aria-label='Version'")
            !contains("<label for='global-search-source-filter'>")
            !contains("<label for='global-search-content-type-filter'>")
            !contains("<label for='global-search-version-filter'>")
            !contains("<button type='submit'>Search</button>")
            contains("<div class='global-search-results-pane' tabindex='-1'>")
            contains("<div class='global-search-fixed-content'>")
            contains("<div class='global-search-results-content'>")
            contains("id='global-search-results' class='global-search-results'")
            contains("id='global-search-load-more' class='global-search-load-more'")
            contains("id='global-search-status' class='global-search-status'")
            contains("class='global-search-close-separator' aria-hidden='true'>&bull;</span>")
            contains("class='global-search-close-hint' data-search-close>Esc to close</button>")
            indexOf("id='global-search-status'") > indexOf("class='global-search-attribution'")
            indexOf("data-search-open") < indexOf("documentation.html")
            contains("class='search-nav-divider'")
            findAll('documentation.html').size() == 1
            contains('<span>App Forge</span>')
            findAll('support.html').size() == 1
            indexOf('blog/index.html') < indexOf('community.html')
            indexOf('blog/index.html') < indexOf('casestudies/index.html')
            indexOf('casestudies/index.html') < indexOf('community.html')
            indexOf('community.html') < indexOf('faq.html')
            indexOf('casestudies/index.html') < indexOf('support.html')
            indexOf('support.html') < indexOf('main-menu')
            contains("href='[%url]/start'")
            indexOf('search.html') < indexOf('/start')
            indexOf('/start') < indexOf('documentation.html')
            indexOf('documentation.html') < indexOf('download.html')
            contains("href='[%url]/guides'>Guides</a>")
            indexOf('download.html') < indexOf('guides')
            indexOf('guides') < indexOf('plugins.html')
            findAll("class='search-nav-divider'").size() == 1
            contains("href='https://algolia.com'")
            contains("target='_blank' rel='noopener noreferrer'")
            contains("aria-label='Search by Algolia'")
            contains("class='global-search-algolia-logo'")
            contains("src='[%url]/images/algolia-logo-blue.svg'")
        }
        with(script) {
            contains("[data-search-open]")
            contains('global-search-input')
            contains('global-search-clear')
            contains('updateClearButton')
            contains("status.textContent = ''")
            contains('global-search-source-filter')
            contains('global-search-content-type-filter')
            contains('global-search-version-filter')
            contains('config.indexName')
            contains("'hierarchy'")
            contains("'guideTitle'")
            contains('Collection:')
            contains('Type:')
            contains('Version:')
            contains("'</p><p class=\"global-search-result-meta\">' + collection")
            !contains('Collection: <strong>')
            !contains('Type: <strong>')
            !contains('Version: <strong>')
            contains("plugin: 'Plugin'")
            contains("contentType === 'plugin'")
            contains("'description'")
            contains("<p class=\"global-search-result-context\">Plugin</p>")
            contains("<p class=\"global-search-result-context\">Blog post</p>")
            contains("<p class=\"global-search-result-context\">Website page</p>")
            contains("<p class=\"global-search-result-context\">API Documentation</p>")
            contains("hit.contentType === 'plugin' && hit.description")
            contains('removeDocumentationTitle')
            contains("hit.source !== 'user-documentation'")
            contains('escapeRegExp')
            contains('prefixPattern')
            contains("title.split('').map")
            contains("(?:<[^>]*>)*")
            contains("const prefixPattern = '^(?:<[^>]*>)*(?:")
            contains("\\\\d+(?:\\\\.\\\\d+)*(?:[.)])?\\\\s+)?")
            contains('Guide:')
            !contains('<strong>Guide:</strong>')
            contains('User Documentation:')
            contains('hierarchyContext')
            !contains('Context:')
            contains('event.preventDefault()')
            contains('DOMContentLoaded')
            contains('contentTypeFilter.value')
            contains('contentTypeOptions')
            contains('updateContentTypeOptions')
            contains('contentTypeFilterContainer.hidden = availableOptions.length === 1')
            contains("sources: ['website']")
            contains("sources: ['guides']")
            contains("sources: ['user-documentation']")
            contains("sources: ['api']")
            contains('sourceFilter.addEventListener(\'change\', function ()')
            contains("versionFilter.value = ''")
            contains('IntersectionObserver')
            contains('loadNextPage')
            contains('insertAdjacentHTML')
            contains('Clear filters...')
            contains('hasActiveFilters')
            contains('clearFilters')
            contains('data-search-clear-filters')
            !contains('global-search-pagination')
            !toLowerCase().contains('google')
        }
        with(template) {
            contains("algolia-config.js")
            contains("algolia-search.css")
            contains("algolia-search.js")
        }
        with(new File(ROOT, 'assets/stylesheets/algolia-search.css').text) {
            contains('height: auto')
            contains('background-color: #fff;')
            contains('color: #7c7c7c;')
            contains("font-family: Roboto, 'Open Sans', 'Helvetica Neue', Helvetica, Arial, sans-serif;")
            contains('.global-search-close-hint:hover')
            contains('background-color: transparent !important;')
            contains('.global-search-close-hint {\n        display: none;')
            contains('@media (hover: none) and (pointer: coarse)')
            contains('border-left: 3px solid #feb672;')
            contains('font-weight: 600;')
            contains('overflow-wrap: anywhere;')
            contains('minmax(0, 1fr)')
            contains('min-width: 0')
            contains('box-shadow: 0 -4px 10px')
            contains('margin-bottom: 4px')
            contains('margin: 6px 0 0')
            contains('.global-search-result-meta')
            contains('font-size: 14px;')
            contains('line-height: 1.5;')
            contains('.global-search-no-results')
            contains('min-height: 160px')
            contains('.global-search-input-control .global-search-clear[hidden]')
            contains('.global-search-mobile-close {\n    display: none;')
            contains('.global-search-status:not(:empty) + .global-search-close-separator')
        }
        logo.isFile()
    }
}

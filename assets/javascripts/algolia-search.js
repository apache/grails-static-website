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
(function () {
    'use strict'

    function initialize() {
        const config = window.GRAILS_ALGOLIA_CONFIG || {}
        const dialog = document.getElementById('global-search-dialog')
        const triggers = document.querySelectorAll('[data-search-open]')
        const form = document.getElementById('global-search-form')
        const input = document.getElementById('global-search-input')
        const clearButton = document.getElementById('global-search-clear')
        const sourceFilter = document.getElementById('global-search-source-filter')
        const contentTypeFilter = document.getElementById('global-search-content-type-filter')
        const contentTypeFilterContainer = document.getElementById('global-search-content-type-filter-container')
        const versionFilter = document.getElementById('global-search-version-filter')
        const status = document.getElementById('global-search-status')
        const results = document.getElementById('global-search-results')
        const resultsPane = document.querySelector('.global-search-results-pane')
        const loadMore = document.getElementById('global-search-load-more')
        let currentPage = 0
        let requestSequence = 0
        let debounceTimer
        let returnFocus
        let loading = false
        let hasMore = false

        const contentTypeOptions = [
            { value: 'page', label: 'Pages', sources: ['website'] },
            { value: 'plugin', label: 'Plugins', sources: ['website'] },
            { value: 'blog', label: 'Blog posts', sources: ['website'] },
            { value: 'faq', label: 'FAQ', sources: ['website'] },
            { value: 'case-study', label: 'Case studies', sources: ['website'] },
            { value: 'guide', label: 'Guides', sources: ['guides'] },
            { value: 'guide-catalog', label: 'Guide catalogue', sources: ['guides'] },
            { value: 'user-documentation', label: 'User Documentation', sources: ['user-documentation'] },
            { value: 'api', label: 'API Documentation', sources: ['api'] }
        ]

        if (!dialog || !form || !input || !status || !results || !resultsPane || !loadMore) return

        const configured = Boolean(config.appId && config.searchApiKey && config.indexName)
        const endpoint = configured ? 'https://' + config.appId + '-dsn.algolia.net/1/indexes/' +
            encodeURIComponent(config.indexName) + '/query' : ''

        function openSearch() {
            returnFocus = document.activeElement
            dialog.hidden = false
            document.body.classList.add('global-search-open')
            triggers.forEach(function (button) { button.setAttribute('aria-expanded', 'true') })
            window.setTimeout(function () { input.focus() }, 0)
            if (!configured) status.textContent = 'Search is not currently configured.'
        }

        function closeSearch() {
            dialog.hidden = true
            document.body.classList.remove('global-search-open')
            triggers.forEach(function (button) { button.setAttribute('aria-expanded', 'false') })
            if (returnFocus && returnFocus.focus) {
                returnFocus.focus()
            }
        }

        function runSearch() {
            const query = input.value.trim()
            if (!query) {
                results.innerHTML = ''
                currentPage = 0
                hasMore = false
                status.textContent = ''
                return
            }
            if (!configured) {
                status.textContent = 'Search is not currently configured.'
                return
            }

            const sequence = ++requestSequence
            loading = false
            hasMore = false
            status.textContent = 'Searching...'
            results.innerHTML = ''
            fetchPage(sequence, 0, false, query)
        }

        function fetchPage(sequence, page, append, query) {
            loading = true
            const filters = []
            if (sourceFilter.value) filters.push('source:' + quote(sourceFilter.value))
            if (contentTypeFilter.value) filters.push('contentType:' + quote(contentTypeFilter.value))
            if (versionFilter.value) filters.push('grailsVersion:' + quote(versionFilter.value))

            fetch(endpoint, {
                method: 'POST',
                headers: {
                    'X-Algolia-Application-Id': config.appId,
                    'X-Algolia-API-Key': config.searchApiKey,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    query: query,
                    page: page,
                    hitsPerPage: 8,
                    attributesToRetrieve: ['title', 'content', 'description', 'url', 'source', 'contentType', 'grailsVersion', 'guideTitle', 'hierarchy'],
                    attributesToHighlight: ['title', 'content'],
                    attributesToSnippet: ['content:30'],
                    facets: ['grailsVersion'],
                    filters: filters.join(' AND ')
                })
            }).then(function (response) {
                if (!response.ok) throw new Error('Search request failed')
                return response.json()
            }).then(function (data) {
                if (sequence !== requestSequence) return
                populateVersions(data.facets && data.facets.grailsVersion)
                currentPage = page
                hasMore = page + 1 < (data.nbPages || 0)
                render(data, append)
                loading = false
            }).catch(function () {
                if (sequence !== requestSequence) return
                loading = false
                hasMore = false
                if (!append) results.innerHTML = ''
                status.textContent = 'Search is temporarily unavailable. Please try again.'
            })
        }

        function loadNextPage() {
            if (loading || !hasMore || !input.value.trim()) return
            status.textContent = 'Loading more results...'
            fetchPage(requestSequence, currentPage + 1, true, input.value.trim())
        }

        function scheduleSearch() {
            window.clearTimeout(debounceTimer)
            currentPage = 0
            debounceTimer = window.setTimeout(runSearch, 250)
        }

        function updateClearButton() {
            if (clearButton) clearButton.hidden = !input.value
        }

        function populateVersions(facets) {
            if (!facets) return
            const selected = versionFilter.value
            const versions = Object.keys(facets).sort(compareVersions).reverse()
            versionFilter.innerHTML = '<option value="">All versions</option>'
            versions.forEach(function (version) {
                const option = document.createElement('option')
                option.value = version
                option.textContent = version
                versionFilter.appendChild(option)
            })
            versionFilter.value = versions.indexOf(selected) >= 0 ? selected : ''
        }

        function updateContentTypeOptions() {
            const selected = contentTypeFilter.value
            const source = sourceFilter.value
            const availableOptions = contentTypeOptions.filter(function (option) {
                return !source || option.sources.includes(source)
            })
            contentTypeFilter.innerHTML = '<option value="">All types</option>'
            availableOptions.forEach(function (option) {
                const element = document.createElement('option')
                element.value = option.value
                element.textContent = option.label
                contentTypeFilter.appendChild(element)
            })
            contentTypeFilterContainer.hidden = availableOptions.length === 1
            contentTypeFilter.value = availableOptions.some(function (option) {
                return option.value === selected
            }) ? selected : ''
        }

        function render(data, append) {
            const hits = data.hits || []
            status.textContent = data.nbHits === 1 ? '1 result found' : data.nbHits + ' results found'
            if (!hits.length && !append) {
                results.innerHTML = '<div class="global-search-no-results"><p>No results found.</p>' +
                    (hasActiveFilters() ? '<a href="#" data-search-clear-filters>Clear filters...</a>' : '') + '</div>'
            } else if (hits.length) {
                const html = hits.map(function (hit) {
                    const title = hit._highlightResult && hit._highlightResult.title ?
                        hit._highlightResult.title.value : escapeHtml(hit.title || 'Untitled')
                    const snippet = hit.contentType === 'plugin' && hit.description ? escapeHtml(hit.description) :
                        hit._snippetResult && hit._snippetResult.content ?
                        removeDocumentationTitle(hit._snippetResult.content.value, hit) :
                        removeDocumentationTitle(escapeHtml(hit.content || ''), hit)
                    const collection = '<span class="global-search-result-badge global-search-result-collection">Collection: ' + escapeHtml(collectionFor(hit)) + '</span>'
                    const type = '<span class="global-search-result-badge global-search-result-type">Type: ' + escapeHtml(typeFor(hit)) + '</span>'
                    const version = hit.grailsVersion ? '<span class="global-search-result-badge global-search-result-version">Version: ' + escapeHtml(hit.grailsVersion) + '</span>' : ''
                    const context = hierarchyContext(hit)
                    return '<article class="global-search-result">' + context + '<h3><a href="' + escapeHtml(hit.url) + '">' + title + '</a></h3>' +
                        '<p>' + snippet + '</p><p class="global-search-result-meta">' + collection + type + version + '</p></article>'
                }).join('')
                if (append) {
                    results.insertAdjacentHTML('beforeend', html)
                } else {
                    results.innerHTML = html
                }
            }
        }

        function hasActiveFilters() {
            return Boolean(sourceFilter.value || contentTypeFilter.value || versionFilter.value)
        }

        function clearFilters() {
            sourceFilter.value = ''
            contentTypeFilter.value = ''
            versionFilter.value = ''
            scheduleSearch()
        }

        function collectionFor(hit) {
            return { website: 'Website', guides: 'Guides', 'user-documentation': 'User Documentation', api: 'API Documentation' }[hit.source] || 'Content'
        }

        function typeFor(hit) {
            return {
                page: 'Page', plugin: 'Plugin', blog: 'Blog', faq: 'FAQ', 'case-study': 'Case Study', guide: 'Guide',
                'guide-catalog': 'Guide Catalogue', 'user-documentation': 'User Documentation', api: 'API'
            }[hit.contentType] || hit.contentType || 'Content'
        }

        function hierarchyContext(hit) {
            if (hit.source === 'website' && hit.contentType === 'plugin') {
                return '<p class="global-search-result-context">Plugin</p>'
            }
            if (hit.source === 'website' && hit.contentType === 'blog') {
                return '<p class="global-search-result-context">Blog post</p>'
            }
            if (hit.source === 'website' && hit.contentType === 'page') {
                return '<p class="global-search-result-context">Website page</p>'
            }
            if (hit.source === 'api') {
                return '<p class="global-search-result-context">API Documentation</p>'
            }
            if (!['guides', 'user-documentation'].includes(hit.source) || !hit.hierarchy) return ''
            const levels = Object.keys(hit.hierarchy).sort(function (a, b) {
                return Number(a.replace('lvl', '')) - Number(b.replace('lvl', ''))
            })
            const parents = levels.slice(0, -1).map(function (level) { return hit.hierarchy[level] }).filter(Boolean)
            if (hit.source === 'guides') {
                const guideTitle = hit.guideTitle || parents.shift()
                if (!guideTitle) return ''
                const sectionParents = parents.filter(function (parent) { return parent !== guideTitle })
                return '<p class="global-search-result-context">Guide: ' +
                    [guideTitle].concat(sectionParents).map(escapeHtml).join(' &gt; ') + '</p>'
            }
            if (!parents.length) return ''
            return '<p class="global-search-result-context">User Documentation: ' +
                parents.map(escapeHtml).join(' &gt; ') + '</p>'
        }

        function removeDocumentationTitle(snippet, hit) {
            if (hit.source !== 'user-documentation' || !hit.title) return snippet
            const title = hit.title.trim().replace(/^\d+(?:\.\d+)*(?:[.)])?\s+/, '')
            const titlePattern = title.split('').map(function (character) {
                return /\s/.test(character) ? '\\s+' : escapeRegExp(escapeHtml(character))
            }).join('(?:<[^>]*>)*')
            const prefixPattern = '^(?:<[^>]*>)*(?:\\d+(?:\\.\\d+)*(?:[.)])?\\s+)?(?:<[^>]*>)*'
            const suffixPattern = '(?:<[^>]*>)*(?:\\s*[-:|.]\\s*|\\s+)'
            const pattern = new RegExp(prefixPattern + titlePattern + suffixPattern, 'i')
            return snippet.replace(pattern, '')
        }

        function escapeRegExp(value) {
            return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
        }

        function quote(value) { return '"' + value.replace(/"/g, '\\"') + '"' }
        function escapeHtml(value) {
            const div = document.createElement('div')
            div.textContent = value == null ? '' : String(value)
            return div.innerHTML
        }
        function compareVersions(a, b) {
            return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' })
        }

        triggers.forEach(function (button) { button.addEventListener('click', function (event) {
            event.preventDefault()
            openSearch()
        }) })
        dialog.querySelectorAll('[data-search-close]').forEach(function (button) {
            button.addEventListener('click', closeSearch)
        })
        form.addEventListener('submit', function (event) {
            event.preventDefault()
            window.clearTimeout(debounceTimer)
            currentPage = 0
            runSearch()
        })
        input.addEventListener('input', scheduleSearch)
        input.addEventListener('input', updateClearButton)
        if (clearButton) clearButton.addEventListener('click', function () {
            input.value = ''
            updateClearButton()
            scheduleSearch()
            input.focus()
        })
        results.addEventListener('click', function (event) {
            const link = event.target.closest('[data-search-clear-filters]')
            if (!link) return
            event.preventDefault()
            clearFilters()
        })
        sourceFilter.addEventListener('change', function () {
            updateContentTypeOptions()
            versionFilter.value = ''
            scheduleSearch()
        })
        contentTypeFilter.addEventListener('change', scheduleSearch)
        versionFilter.addEventListener('change', scheduleSearch)
        updateContentTypeOptions()
        if ('IntersectionObserver' in window) {
            new IntersectionObserver(function (entries) {
                if (entries.some(function (entry) { return entry.isIntersecting })) loadNextPage()
            }, { root: resultsPane, rootMargin: '0px 0px 240px 0px' }).observe(loadMore)
        }
        document.addEventListener('keydown', function (event) {
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
                event.preventDefault()
                openSearch()
            } else if (event.key === 'Escape' && !dialog.hidden) {
                closeSearch()
            }
        })

        if (window.location.pathname.endsWith('/search.html')) openSearch()
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize)
    } else {
        initialize()
    }
}())

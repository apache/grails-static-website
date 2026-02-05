const queryInputFieldId = 'query'
const mobileQueryInputFieldId = 'mobile-query'
const allPluginsContainerDivClass = 'all-plugins'
const pluginContainerDivClassName = 'plugin'
const allPluginsHeadingLabelClassName = 'all-plugins-label'
const searchResultsDivClassName = 'search-results'
const searchResultsHeadingLabelClassName = 'search-results-label'
const searchResultsLabelSelector = 'h3.' + searchResultsHeadingLabelClassName
const gitHubStarsSelector = 'div.github-star'
const noresultsDivClassName = 'no-results'
const allPlugins = []
const elementsClassNames = [allPluginsContainerDivClass, allPluginsHeadingLabelClassName]

// Tab navigation
document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.querySelectorAll('.plugins-nav .nav-tab')
    const tabContents = document.querySelectorAll('.tab-content')
    tabs.forEach(tab => {
        tab.addEventListener('click', e => {
            e.preventDefault()

            // Remove the active class from all tabs and contents
            tabs.forEach(t => t.classList.remove('active'))
            tabContents.forEach(c => c.classList.remove('active'))

            // Add active class to clicked tab
            tab.classList.add('active')

            // Show corresponding content
            const tabId = tab.getAttribute('data-tab')
            const content = document.getElementById(tabId)
            if (content) {
                content.classList.add('active')
            }

            // Update URL hash
            history.replaceState(null, null, '#' + tabId)
        })
    })

    // Handle initial hash on page load
    const hash = window.location.hash.substring(1)
    if (hash) {
        const tab = document.querySelector('.nav-tab[data-tab="' + hash + '"]')
        if (tab) {
            tab.click()
        }
    }
})

window.addEventListener('load', () => {
    const elements = document.querySelectorAll(`div.${allPluginsContainerDivClass} ul > li.plugin`)
    for (let i = 0; i <= elements.length - 1; i++) {
        const pluginData = elements[i].innerHTML
        const name = elements[i].getElementsByClassName('name')
        const desc = elements[i].getElementsByClassName('desc')[0]?.textContent
        const owner = elements[i].getElementsByClassName('owner')
        const labels = elements[i].getElementsByClassName('label')
        const vcsUrl = elements[i].querySelector('h3.name > a').href
        const metaInfo = elements[i].querySelector("p")?.outerHTML
        const ghStar = elements[i].querySelector(gitHubStarsSelector)?.outerHTML
        // Collect all Grails versions from the version dropdown items
        const grailsCompatElements = elements[i].querySelectorAll('.grails-compat, .compat')
        const allGrailsVersions = [...grailsCompatElements].map(el => el.textContent).filter(v => v)
        allPlugins.push({
            pluginData: pluginData,
            name: name[0]?.textContent,
            desc: desc,
            owner: owner[0]?.textContent,
            labels: labelsAtPlugin(labels),
            vcsUrl: vcsUrl,
            metaInfo: metaInfo,
            ghStar: ghStar,
            grailsVersions: allGrailsVersions,
            element: elements[i]
        })
    }

    if (document.getElementById(queryInputFieldId)) {
        const queryInput = document.getElementById(queryInputFieldId)
        const searchBox = queryInput.closest('.search-box-inline')
        const clearBtn = searchBox?.querySelector('.search-clear-btn')

        queryInput.addEventListener('input', onFilterChanged)

        // Update the has-value class on input
        queryInput.addEventListener('input', () => {
            if (searchBox) {
                searchBox.classList.toggle('has-value', queryInput.value.length > 0)
            }
        })

        // Clear button functionality
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                queryInput.value = ''
                searchBox.classList.remove('has-value')
                onFilterChanged()
                queryInput.focus()
            })
        }
    }
    if (document.getElementById(mobileQueryInputFieldId)) {
        document.getElementById(mobileQueryInputFieldId)
            .addEventListener('input', onFilterChanged)
    }

    const grailsVersionSelect = document.getElementById('grails-version-select')
    if (grailsVersionSelect) {
        grailsVersionSelect.addEventListener('change', onFilterChanged)
    }
});

function hideElementsToDisplaySearchResults() {
    for (let i = 0; i < elementsClassNames.length; i++) {
        hideElementsByClassName(elementsClassNames[i])
    }
}

function resetDefault() {
    hideElementsByClassName(noresultsDivClassName)
    hideElementsByClassName(searchResultsDivClassName)
    hideElementsByClassName(searchResultsHeadingLabelClassName)
    clearSearchResultsDiv()
    for (let i = 0; i < elementsClassNames.length; i++) {
        showElementsByClassName(elementsClassNames[i])
    }
    paginate(defaultPluginList, max, pluginsContainer, paginationContainerClass)
}

function hideElementsByClassName(className) {
    const elements = document.getElementsByClassName(className)
    for (let i = 0; i < elements.length; i++) {
        elements[i].classList.add('hidden')
    }
}

function showElementsByClassName(className) {
    const elements = document.getElementsByClassName(className)
    for (let i = 0; i < elements.length; i++) {
        elements[i].classList.remove('hidden')
    }
}

const labelsAtPlugin = elements =>
    [...elements].map(el => el.textContent);

function clearSearchResultsDiv() {
    searchResultsDiv.innerHTML = ''
}

function getSelectedGrailsVersion() {
    const select = document.getElementById('grails-version-select')
    return select ? select.value : ''
}

function doesGrailsVersionMatch(plugin, majorVersion) {
    if (!majorVersion) return true // No filter = show all
    const grailsVersions = plugin.grailsVersions || []
    if (grailsVersions.length === 0) return false
    // Check if any version matches the major version
    return grailsVersions.some(version => {
        return version.startsWith(majorVersion + '.') || version === majorVersion
    })
}

function onFilterChanged() {
    const query = (queryValue() || '').trim()
    const grailsVersion = getSelectedGrailsVersion()

    // If no filters are active, show default
    if (!query && !grailsVersion) {
        resetDefault()
        return
    }

    // For search query, require at least 2 characters
    if (query && query.length < 2 && !grailsVersion) {
        return
    }

    // Filter plugins based on both search query and Grails version
    const matchingPlugins = allPlugins.filter(plugin => {
        const matchesQuery = !query || query.length < 2 || doesPluginMatchQuery(plugin, query)
        const matchesVersion = doesGrailsVersionMatch(plugin, grailsVersion)
        return matchesQuery && matchesVersion
    })

    if (searchResultsDiv) {
        if (matchingPlugins.length > 0) {
            hideElementsToDisplaySearchResults()

            // Update search results heading
            const searchResultHeadingLabel = document.querySelector(searchResultsLabelSelector)
            if (searchResultHeadingLabel) {
                const querySpan = searchResultHeadingLabel.querySelector('span')
                let filterText = []
                if (query && query.length >= 2) filterText.push(`"${query}"`)
                if (grailsVersion) filterText.push(`Grails ${grailsVersion}.x`)
                querySpan.innerHTML = filterText.join(' + ')
                showElementsByClassName(searchResultsHeadingLabelClassName)
            }

            searchResultsDiv.innerHTML = renderPlugins(matchingPlugins)
            const searchResultsList = searchResultsDiv.querySelector('ul.plugin-list')
            paginate(Array.from(searchResultsList.getElementsByClassName(pluginContainerDivClassName)), max, searchResultsList, paginationContainerClass)
            showElementsByClassName(searchResultsDivClassName)
            hideElementsByClassName(noresultsDivClassName)
        } else {
            clearSearchResultsDiv()
            hideElementsToDisplaySearchResults()
            showElementsByClassName(noresultsDivClassName)
            hideElementsByClassName(searchResultsHeadingLabelClassName)
            const pagination = document.querySelector(paginationContainerClass)
            if (pagination) pagination.innerHTML = ''
        }
    }
}

function doesTagsMatchQuery(tags, query) {
    const q = query.toLowerCase()
    return tags.some(tag => tag.toLowerCase().includes(q))
}

function doesTitleMatchQuery(title, query) {
    if (title == null) return false
    const t = title.toLowerCase()
    const q = query.toLowerCase()
    return t.includes(q) || (q.includes(" ") && q.split(" ").every(term => t.includes(term)))
}

function doesOwnerMatchQuery(owner, query) {
    return owner != null && owner.toLowerCase().includes(query.toLowerCase())
}

function doesPluginMatchQuery(guide, query) {
    return doesTitleMatchQuery(guide.name, query) || doesOwnerMatchQuery(guide.owner, query) || doesTagsMatchQuery(guide.labels, query)
}

function queryValue() {
    const val = id => (document.getElementById(id)?.value ?? '').trim()
    return val(queryInputFieldId) || val(mobileQueryInputFieldId)
}

function renderPlugins(plugins) {
    return `<ul class="plugin-list">${plugins.map(p => renderPluginAsHtmlLi(p)).join('')}</ul>`
}

function renderPluginAsHtmlLi(plugin) {
    return `<li class="plugin">${plugin.pluginData}</li>`
}
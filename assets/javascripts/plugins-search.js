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
        allPlugins.push({
            pluginData: pluginData,
            name: name[0]?.textContent,
            desc: desc,
            owner: owner[0]?.textContent,
            labels: labelsAtPlugin(labels),
            vcsUrl: vcsUrl,
            metaInfo: metaInfo,
            ghStar: ghStar
        })
    }

    if (document.getElementById(queryInputFieldId)) {
        const queryInput = document.getElementById(queryInputFieldId)
        const searchBox = queryInput.closest('.search-box-inline')
        const clearBtn = searchBox?.querySelector('.search-clear-btn')

        queryInput.addEventListener('input', onQueryChanged)

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
                resetDefault()
                queryInput.focus()
            })
        }
    }
    if (document.getElementById(mobileQueryInputFieldId)) {
        document.getElementById(mobileQueryInputFieldId)
            .addEventListener('input', onQueryChanged)
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

function onQueryChanged() {
    let query = queryValue()?.trim()
    const matchingPlugins = []
    if (query === null || query === '') {
        resetDefault()
        return
    } else if (query.length < 3) {
        return
    }

    if (query !== '') {
        for (let i = 0; i <= allPlugins.length - 1; i++) {
            const plugin = allPlugins[i]
            if (doesPluginMatchesQuery(plugin, query)) {
                matchingPlugins.push(plugin)
            }
        }
    }
    if (searchResultsDiv) {
        if (matchingPlugins.length > 0) {
            if (searchResultsDiv.parentNode.getElementsByClassName(searchResultsLabelSelector).length === 0) {
                const searchResultHeadingLabel = document.querySelector(searchResultsLabelSelector)
                const querySpan = searchResultHeadingLabel.querySelector('span')
                querySpan.innerHTML = queryValue()
                showElementsByClassName(searchResultsHeadingLabelClassName)
            }
            hideElementsToDisplaySearchResults()
            searchResultsDiv.innerHTML = renderPlugins(matchingPlugins)
            paginate(Array.from(searchResultsDiv.getElementsByClassName(pluginContainerDivClassName)), max, searchResultsDiv, paginationContainerClass)
            showElementsByClassName(searchResultsDivClassName)
            hideElementsByClassName(noresultsDivClassName)
        } else if (matchingPlugins.length === 0) {
            clearSearchResultsDiv()
            hideElementsToDisplaySearchResults()
            showElementsByClassName(noresultsDivClassName)
            const pagination = document.querySelector(paginationContainerClass)
            pagination.innerHTML = ''
        }
    }
}

function doesTagsMatchesQuery(tags, query) {
    const q = query.toLowerCase()
    return tags.some(tag => tag.toLowerCase().includes(q))
}

function doesTitleMatchesQuery(title, query) {
    if (title == null) return false
    const t = title.toLowerCase()
    const q = query.toLowerCase()
    return t.includes(q) || (q.includes(" ") && q.split(" ").every(term => t.includes(term)))
}

function doesOwnerMatchesQuery(owner, query) {
    return owner != null && owner.toLowerCase().includes(query.toLowerCase())
}

function doesPluginMatchesQuery(guide, query) {
    return doesTitleMatchesQuery(guide.name, query) || doesOwnerMatchesQuery(guide.owner, query) || doesTagsMatchesQuery(guide.labels, query)
}

function queryValue() {
    const val = id => (document.getElementById(id)?.value ?? '').trim()
    return val(queryInputFieldId) || val(mobileQueryInputFieldId)
}

function renderPlugins(plugins) {
    return `  <ul>${plugins.map(p => `    ${renderPluginAsHtmlLi(p)}`).join('')}  </ul>`
}

function renderPluginAsHtmlLi(plugin) {
    return `<li class="plugin">${plugin.pluginData}</li>`
}
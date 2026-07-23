const allGuides = []
const guideClassName = 'guide'
const mobileQueryInputFieldId = 'mobile-query'
const multiGuideClassName = 'multi-guide'
const queryInputFieldId = 'query'
const searchStatusId = 'guides-search-status'


const elementsClassNames = [
    'training',
    'latest-guides',
    'guide-group',
    'versions-by-grails',
    'tags-by-topic',
    'guides-suggestion',
    'guides-catalogue',
]

window.addEventListener('load', () => {
    let elements = document.getElementsByClassName(guideClassName)
    for (let i = 0; i < elements.length; i++) {
        allGuides.push({
            href: elements[i].getAttribute('href'),
            title: elements[i].text,
            tags: tagsAtGuide(elements[i].parentNode)
        })
    }
    elements = document.getElementsByClassName(multiGuideClassName)
    for (let i = 0; i < elements.length; i++) {
        allGuides.push({
            title: titleAtMultiGuide(elements[i]),
            versions: versionsAtMultiGuide(elements[i])
        })
    }
    if (document.getElementById(queryInputFieldId)) {
        document.getElementById(queryInputFieldId)
            .addEventListener('input', onQueryChanged)
    }
    if (document.getElementById(mobileQueryInputFieldId)) {
        document.getElementById(mobileQueryInputFieldId)
            .addEventListener('input', onQueryChanged)
    }
})

function hideElementsToDisplaySearchResults() {
    for (let i = 0; i < elementsClassNames.length; i++) {
        hideElementsByClassName(elementsClassNames[i])
    }
}

function showElementsToDisplaySearchResults() {
    for (let i = 0; i < elementsClassNames.length; i++) {
        showElementsByClassName(elementsClassNames[i])
    }
}

function hideElementsByClassName(className) {
    const elements = document.getElementsByClassName(className)
    for (let i = 0; i < elements.length; i++) {
        elements[i].style.display = 'none'
    }
}

function showElementsByClassName(className) {
    const elements = document.getElementsByClassName(className)
    for (let i = 0; i < elements.length; i++) {
        elements[i].style.display = ''
    }
}

function hasClass(node, className) {
    if (!node || node.nodeType !== 1) {
        return false
    }
    if (node.classList) {
        return node.classList.contains(className)
    }
    const value = node.className || ''
    return (' ' + value + ' ').indexOf(' ' + className + ' ') !== -1
}

function updateSearchStatus(message) {
    const status = document.getElementById(searchStatusId)
    if (status) {
        status.textContent = message
    }
}

function titleAtMultiGuide(element) {
    for (let y = 0; y < element.childNodes.length; y++) {
        if (hasClass(element.childNodes[y], 'title')) {
            return element.childNodes[y].textContent
        }
    }
    return ''
}

function versionsAtMultiGuide(element) {
    const versions = []
    for (let y = 0; y < element.childNodes.length; y++) {
        if (hasClass(element.childNodes[y], 'align-left')) {
            const versionDiv = element.childNodes[y]
            let verEl
            let hrefEl
            const tagsArr = []
            for (let x = 0; x < versionDiv.childNodes.length; x++) {
                if (hasClass(versionDiv.childNodes[x], 'grails-version')) {
                    verEl = versionDiv.childNodes[x].textContent
                    hrefEl = versionDiv.childNodes[x].getAttribute('href')
                }
                if (hasClass(versionDiv.childNodes[x], 'tag')) {
                    tagsArr.push(versionDiv.childNodes[x].textContent);
                }
            }
            versions.push({
                grailsVersion: verEl,
                href: hrefEl,
                tags: tagsArr
            })
        }
    }
    return versions
}

function tagsAtGuide(element) {
    const tags = []
    for (let y = 0; y < element.childNodes.length; y++) {
        if (hasClass(element.childNodes[y], 'tag')) {
            tags.push(element.childNodes[y].textContent);
        }
    }
    return tags
}

function onQueryChanged() {
    const query = queryValue().trim()
    const resultsDiv = document.getElementsByClassName('search-results')
    if (!resultsDiv.length) {
        return
    }
    if (query === '') {
        showElementsToDisplaySearchResults()
        resultsDiv[0].innerHTML = ''
        updateSearchStatus('')
        return
    }

    // Hide catalogue and other browse surfaces for every non-empty query
    // before branching into matches vs no-results.
    hideElementsToDisplaySearchResults()

    const matchingGuides = []
    for (let i = 0; i < allGuides.length; i++) {
        let guide = allGuides[i]
        if (doesGuideMatchQuery(guide, query) ) {
            matchingGuides.push(guide)
        }
    }
    if (matchingGuides.length > 0) {
        resultsDiv[0].innerHTML = renderGuideGroup(matchingGuides, query)
        const count = matchingGuides.length
        updateSearchStatus(count === 1 ? '1 guide found' : count + ' guides found')
    } else {
        resultsDiv[0].innerHTML =
            "<div class='guide-group'><div class='guide-group-header'><h3>No results found</h3></div></div>"
        updateSearchStatus('No results found')
    }
}

function doesTagsMatchQuery(tags, query) {
    for (let x = 0; x < tags.length; x++) {
        if (tags[x].toLowerCase().indexOf(query.toLowerCase()) !== -1) {
            return true
        }
    }
    return false
}

function doesTitleMatchQuery(title, query) {
    if (title.toLowerCase().indexOf(query.toLowerCase()) !== -1) {
        return true
    }
}

function doesGuideMatchQuery(guide, query) {
    if (doesTitleMatchQuery(guide.title, query)) {
        return true
    }
    if (guide.tags === undefined || guide.tags === null) {
        for (let i = 0; i < guide.versions.length; i++) {
            const version = guide.versions[i]
            if (doesTagsMatchQuery(version.tags, query)) {
                return true
            }
        }
    } else {
        if (doesTagsMatchQuery(guide.tags, query)) {
            return true
        }
    }
    return false
}

function queryValue() {
    const value = document.getElementById(queryInputFieldId).value.trim()
    if (value === '') {
        if (document.getElementById(mobileQueryInputFieldId)) {
            return document.getElementById(mobileQueryInputFieldId).value.trim()
        }
    }
    return value
}

function renderGuideGroup(guides, query) {
    const items = guides.map(g => renderGuideAsHtmlLi(g, query)).join('');
    return `
    <div class="guide-group">
      <div class="guide-group-header">
        <h3>Guides Filtered by: ${queryValue()}</h3>
      </div>
      <ul>
        ${items}
      </ul>
    </div>
  `;
}

function renderGuideAsHtmlLi(guide, query) {
    const hiddenTags = (tags = []) =>
        tags.map(tag => `<span class="tag">${tag}</span>`).join('')

    // Multi-guide (no guide.tags)
    if (guide.tags == null) {
        const titleMatched = doesTitleMatchQuery(guide.title, query)
        const versionsHtml = guide.versions
            .filter(v => titleMatched || doesTagsMatchQuery(v.tags, query))
            .map(v => `
              <div class="align-left guides-version-chip">
                <a class="grails-version" href="${v.href}">${v.grailsVersion}</a>
                ${hiddenTags(v.tags)}
              </div>`
            )
            .join('')

        return `<li>
                  <div class="multi-guide">
                    <span class="title">${guide.title}</span>
                    ${versionsHtml}
                  </div>
                </li>`;
    }

    // Single guide (has guide.tags)
    return `<li>
              <a class="${guideClassName}" href="${guide.href}">${guide.title}</a>
              ${hiddenTags(guide.tags)}
            </li>`;
}

let pluginsDiv
let defaultPluginList
let pluginsContainer
let queryInputOffsetTop = document.getElementById('query')
let searchResultsDiv

window.addEventListener('load', () => {
    searchResultsDiv = document.querySelector('div.search-results')
    pluginsDiv = document.getElementsByClassName('all-plugins')
    // Target the ul.plugin-list inside the plugins div, not the div itself
    pluginsContainer = document.querySelector('div.plugins ul.plugin-list')
    if (queryInputOffsetTop !== null) {
        queryInputOffsetTop = queryInputOffsetTop.offsetTop
    }
    if (pluginsDiv.length > 0 && pluginsDiv[0].display !== 'none') {
        defaultPluginList = Array.from(pluginsDiv[0].getElementsByClassName('plugin')).slice()
        paginate(defaultPluginList, max, pluginsContainer, paginationContainerClass)
    }
})

// Copy Maven coordinates to clipboard
document.addEventListener('click', async (e) => {
    const btn = e.target.closest('.copy-btn')
    if (!btn) return

    const coords = btn.dataset.coords
    if (!coords) return

    const flash = () => {
        const original = btn.textContent
        btn.textContent = '✓'
        btn.classList.add('copied')
        setTimeout(() => {
            btn.textContent = original
            btn.classList.remove('copied')
        }, 1500)
    }

    const fallbackCopy = (text) => {
        const ta = document.createElement('textarea')
        ta.value = text
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        ta.remove()
    }

    try {
        await navigator.clipboard.writeText(coords)
        flash()
    } catch (err) {
        console.error('Failed to copy:', err)
        fallbackCopy(coords)
        flash()
    }
})

// Version dropdown click-to-toggle
document.addEventListener('click', (e) => {
    const dropdown = e.target.closest('.version-current')?.closest('.version-dropdown')
    // Click outside any .version-current -> close all
    if (!dropdown) {
        document.querySelectorAll('.version-dropdown.open')
            .forEach(d => d.classList.remove('open'))
        return
    }
    e.stopPropagation()
    // Close other open dropdowns
    document.querySelectorAll('.version-dropdown.open').forEach(d => {
        if (d !== dropdown) d.classList.remove('open')
    })
    dropdown.classList.toggle('open')
})

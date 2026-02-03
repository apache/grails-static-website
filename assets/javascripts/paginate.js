const max = 20
const paginationContainerClass = '.pagination-container'

function paginate(items, itemsPerPage, itemsContainer, paginationContainer) {
    let currentPage = 1
    const totalPages = Math.ceil(items.length / itemsPerPage)

    function showItems(page) {
        const startIndex = (page - 1) * itemsPerPage
        const endIndex = startIndex + itemsPerPage
        const pageItems = items.slice(startIndex, endIndex)

        itemsContainer.innerHTML = ""

        pageItems.forEach((item) => {
            const li = document.createElement('li')
            li.innerText = item.innerText
            itemsContainer.appendChild(item)
        })
    }

    function createPaginationLinks(paginationElement) {
        const isBottomPagination = paginationElement.classList.contains('bottom')
        paginationElement.innerHTML = ''
        if (totalPages > 1) {
            for (let i = 1; i <= totalPages; i++) {
                const link = document.createElement('a')
                link.href = 'javascript:void(0)'
                link.innerText = `${i}`
                if (i === currentPage) {
                    link.classList.add('active')
                }
                link.addEventListener('click', (event) => {
                    event.preventDefault()
                    currentPage = i

                    // Calculate distance from bottom of page before redraw (only for bottom pagination)
                    const scrollBottomOffset = isBottomPagination
                        ? document.documentElement.scrollHeight - window.scrollY
                        : null

                    showItems(currentPage)

                    // Restore scroll position relative to bottom (only for bottom pagination)
                    if (isBottomPagination) {
                        window.scrollTo(0, document.documentElement.scrollHeight - scrollBottomOffset)
                    }

                    // Update active state on all pagination containers
                    document.querySelectorAll(paginationContainer + ' a.active').forEach(a => a.classList.remove('active'))
                    document.querySelectorAll(paginationContainer + ' a').forEach(a => {
                        if (a.innerText === `${i}`) {
                            a.classList.add('active')
                        }
                    })
                })
                paginationElement.appendChild(link)
            }
        }
    }

    function setupPagination() {
        // Populate all pagination containers (top and bottom)
        const paginationElements = document.querySelectorAll(paginationContainer)
        paginationElements.forEach(el => createPaginationLinks(el))
    }

    showItems(currentPage)
    setupPagination()
}
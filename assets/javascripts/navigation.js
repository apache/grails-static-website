function show(showId, hideId) {
    document.getElementById(showId).style.display = 'block'
    document.getElementById(hideId).style.display = 'none'
}

function toggleNavigation() {
    const menus = document.getElementById('top-menus')
    const toggle = document.getElementById('show-navigation-link')
    if (!menus || !toggle) {
        return
    }
    const isOpen = menus.classList.contains('is-open')
    if (isOpen) {
        menus.classList.remove('is-open')
        toggle.textContent = 'Show Navigation'
        toggle.setAttribute('aria-expanded', 'false')
    } else {
        menus.classList.add('is-open')
        toggle.textContent = 'Hide Navigation'
        toggle.setAttribute('aria-expanded', 'true')
    }
}

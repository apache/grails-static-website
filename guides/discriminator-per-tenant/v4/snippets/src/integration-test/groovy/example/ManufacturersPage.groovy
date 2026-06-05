package example

import geb.Page

class ManufacturersPage extends Page {

    static at = { $('h2').text().contains('Available Manufacturers') }

    static content = {
        audiLink { $('a', text: 'Audi') }
        fordLink { $('a', text: 'Ford') }
    }

    void selectAudi() {
        audiLink.click()
    }

    void selectFord() {
        fordLink.click()
    }
}

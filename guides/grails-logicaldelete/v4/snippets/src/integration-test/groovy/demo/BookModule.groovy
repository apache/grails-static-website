package demo

import geb.Module

class BookModule extends Module {

    static content = {
        deleteButton { $('input', type: 'submit') }
    }

    void delete() {
        deleteButton.click()
    }
}

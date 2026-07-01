package example.grails

import geb.Page

class HomePage extends Page {

    static at = { title == 'Welcome to Grails' }

    static url = '/'

    static content = {
        excelLink { $('a', text: contains('Excel'), 0) }
    }

    void downloadExcel() {
        excelLink.click()
    }
}

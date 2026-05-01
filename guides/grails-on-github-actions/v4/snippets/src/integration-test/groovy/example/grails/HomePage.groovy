package example.grails

import geb.Page

class HomePage extends Page {

    static url = "/"

    static content = {
        titleHeader { $('h1', 0) }
    }

    String getTitle() {
        titleHeader.text()
    }
}

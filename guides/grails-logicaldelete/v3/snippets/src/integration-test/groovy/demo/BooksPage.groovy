package demo

import geb.Page

class BooksPage extends Page {

    static url = '/book'

    static at = { title == 'Groovy & Grails Books'}

    static content = {
        totalNumberOfBooks { $('#totalNumberOfBooks', 0) }
        bookDivs { $('div.book') }
        bookDiv { $('div.book', it).module(BookModule) }
        undoButton(required: false) { $('input', type: 'submit', value: 'Undo') }

    }

    void delete(int i) {
        bookDiv(i).delete()
    }

    void undo() {
        undoButton.click()
    }

    Integer count() {
        Integer.valueOf(totalNumberOfBooks.text())
    }
}

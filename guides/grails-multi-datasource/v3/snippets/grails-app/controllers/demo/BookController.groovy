package demo

import groovy.transform.CompileStatic

@CompileStatic
class BookController {

    static allowedMethods = [save: 'POST', index: 'GET']

    static responseFormats = ['json']

    BookService bookService

    KeywordService keywordService

    BookDataService bookDataService

    def save(SaveBookCommand cmd) {
        bookService.addBook(cmd.title, cmd.keywords)
        render status: 201
    }

    def index() {
        [bookList: bookDataService.findAll()]
    }

    def delete(String title) {
        bookDataService.deleteByTitle(title)
        render status: 204
    }

    def keywords() {
        render view: '/keyword/index',
               model: [keywordList: keywordService.findAllBooksKeywords()]
    }
}
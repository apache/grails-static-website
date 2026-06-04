package example

import grails.converters.JSON
import grails.gorm.transactions.Transactional

class BookController {

    static responseFormats = ['json']
    static allowedMethods = [index: 'GET', show: 'GET', save: 'POST', update: 'PUT', delete: 'DELETE']

    BookService bookService

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Book.list(params), model: [bookCount: Book.count()]
    }

    def show(Long id) {
        respond Book.get(id)
    }

    @Transactional
    def save() {
        def book = new Book(request.JSON as Map)
        if (!book.validate()) {
            respond book.errors, status: 422
            return
        }
        bookService.saveBook(book)
        respond book, status: 201
    }

    @Transactional
    def update(Long id) {
        def book = Book.get(id)
        if (!book) {
            render status: 404
            return
        }
        book.properties = request.JSON
        if (!book.validate()) {
            respond book.errors, status: 422
            return
        }
        bookService.saveBook(book)
        respond book
    }

    @Transactional
    def delete(Long id) {
        def book = Book.get(id)
        if (!book) {
            render status: 404
            return
        }
        book.delete(flush: true)
        render status: 204
    }

    def byAuthor(Long authorId) {
        def author = Author.get(authorId)
        if (!author) {
            render status: 404
            return
        }
        respond bookService.listByAuthor(author)
    }

    def search(String q) {
        respond bookService.searchByTitle(q)
    }
}

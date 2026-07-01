package demo

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class BooksUndoSpec extends GebSpec {

    BookDataService bookDataService

    def "verify a book can be deleted and deletion can be undone"() {
        given:
        Map bookInfo = [
                title: 'Programming Groovy 2',
                author: 'Venkat Subramaniam',
                href: 'http://pragprog.com/book/vslg2/programming-groovy-2',
                about: 'This book helps experienced Java developers learn to use Groovy 2, from the basics of the language to its latest advances.',
                image: 'vslg2.jpg'
        ]
        Book book = bookDataService.save(bookInfo.title, bookInfo.author, bookInfo.about, bookInfo.href, bookInfo.image)

        when:
        BooksPage booksPage = to BooksPage

        then:
        booksPage.count()

        when:
        booksPage.delete(0)

        then:
        bookDataService.count() == old(bookDataService.count()) - 1
        booksPage.count() == (old(booksPage.count()) - 1)

        when:
        booksPage.undo()

        then:
        bookDataService.count() == old(bookDataService.count()) + 1
        booksPage.count() == (old(booksPage.count()) + 1)

        cleanup:
        bookDataService.delete(book.id)
    }
}

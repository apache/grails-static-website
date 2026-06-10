package example

import java.time.LocalDate

class BootStrap {

    def init = { servletContext ->
        environments {
            development {
                Author.withTransaction {
                    if (Author.count() == 0) {
                        def tags = ['gorm', 'grails', 'data-access'].collect { new Tag(name: it).save(failOnError: true) }

                        def author = new Author(name: 'Ada Lovelace', email: 'ada@example.com').save(failOnError: true)

                        def book = new Book(
                            title: 'Computing and Composition',
                            isbn: '978-0000000001',
                            price: 24.99,
                            publishedOn: LocalDate.of(1843, 10, 1),
                            author: author
                        ).save(failOnError: true)

                        tags.each { book.addToTags(it) }
                        book.save(failOnError: true, flush: true)
                    }
                }
            }
        }
    }

    def destroy = {
    }
}

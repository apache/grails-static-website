package example

import grails.util.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BootStrap {

    private static final Logger log = LoggerFactory.getLogger(BootStrap)

    Closure init = { servletContext ->

        if (Environment.current == Environment.PRODUCTION) {
            log.info 'production environment - skipping bootstrap data'
            return
        }
        if (Author.count() > 0) {
            log.info 'authors already present - skipping bootstrap data'
            return
        }

        Author.withTransaction {
            Author tolkien = new Author(name: 'J.R.R. Tolkien',
                                        biography: 'English writer and philologist (1892-1973).',
                                        dateOfBirth: Date.parse('yyyy-MM-dd', '1892-01-03'))
                                        .save(failOnError: true)
            Author leguin = new Author(name: 'Ursula K. Le Guin',
                                       biography: 'American author (1929-2018).',
                                       dateOfBirth: Date.parse('yyyy-MM-dd', '1929-10-21'))
                                       .save(failOnError: true)

            new Book(author: tolkien, title: 'The Hobbit', isbn: '9780547928227', pageCount: 310,
                     publishedOn: Date.parse('yyyy-MM-dd', '1937-09-21')).save(failOnError: true)
            new Book(author: tolkien, title: 'The Fellowship of the Ring', isbn: '9780547928210', pageCount: 423,
                     publishedOn: Date.parse('yyyy-MM-dd', '1954-07-29')).save(failOnError: true)
            new Book(author: leguin, title: 'A Wizard of Earthsea', isbn: '9780547851402', pageCount: 205,
                     publishedOn: Date.parse('yyyy-MM-dd', '1968-11-01')).save(failOnError: true)
            new Book(author: leguin, title: 'The Left Hand of Darkness', isbn: '9780441478125', pageCount: 304,
                     publishedOn: Date.parse('yyyy-MM-dd', '1969-03-01')).save(failOnError: true)
        }
    }

    Closure destroy = { }
}

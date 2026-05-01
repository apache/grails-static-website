package demo

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@Secured('permitAll')
@CompileStatic
class BookController {

    static allowedMethods = [index: 'GET', show: 'GET']

    BookDataService bookDataService

    BookFavouriteDataService bookFavouriteDataService

    SpringSecurityService springSecurityService

    def index() {
        [bookList: bookDataService.findAll()]
    }

    def show(Long id) {
        [
                bookInstance: bookDataService.findById(id),
                bookFavourite: bookFavouriteDataService.findByBookIdAndUsername(id, loggedUsername())
        ]
    }

    @CompileDynamic
    protected String loggedUsername() {
        final String username = springSecurityService.principal?.username
        username
    }
}
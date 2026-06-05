package demo

import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.CompileStatic

@Secured('permitAll')
@CompileStatic
class BookController {

    static allowedMethods = [index: 'GET', show: 'GET']

    BookDataService bookDataService

    def index() {
        [bookList: bookDataService.findAll()]
    }

    def show(Long id) {
        [bookInstance: bookDataService.findById(id)]
    }

}
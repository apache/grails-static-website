package demo

import grails.plugins.elasticsearch.ElasticSearchService
import grails.validation.ValidationException
import groovy.transform.CompileStatic

import static org.springframework.http.HttpStatus.*

@CompileStatic
class BookController {

    BookSearchService bookSearchService

    static responseFormats = ['json']
    static allowedMethods = [
            search: "GET"
    ]

    def search(String q) {
        if ( !q ) {
            render status: NOT_FOUND // <1>
            return
        }
        respond bookSearchService.search(q)
    }
}
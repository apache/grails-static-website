package demo

import grails.plugins.elasticsearch.ElasticSearchService
import groovy.transform.CompileStatic

@CompileStatic
class BookSearchService {
    ElasticSearchService elasticSearchService

    Map search(String query) {
        elasticSearchService.search(query, [indices: Book, types: Book, score: true]) as Map // <1>
    }
}
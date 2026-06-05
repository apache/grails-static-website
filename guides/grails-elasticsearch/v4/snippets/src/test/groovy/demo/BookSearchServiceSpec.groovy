package demo

import grails.plugins.elasticsearch.ElasticSearchService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class BookSearchServiceSpec extends Specification implements ServiceUnitTest<BookSearchService> {

    def "search uses indices and types `Book` and score `true`"() {

        service.elasticSearchService = Mock(ElasticSearchService)

        when:
        service.search('Grails')

        then:
        1 * service.elasticSearchService.search('Grails', [indices: Book, types: Book, score: true])
    }
}

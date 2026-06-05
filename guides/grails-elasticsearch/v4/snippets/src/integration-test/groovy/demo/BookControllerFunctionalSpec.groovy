package demo

import grails.plugins.rest.client.RestResponse
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf

@IgnoreIf( { System.getenv('TRAVIS') as boolean } )
@Integration
class BookControllerFunctionalSpec extends RestSpec {

    BookDataService bookDataService

    void "Test the search action correctly searches"() {
        expect:
        bookDataService.count()

        when:
        RestResponse rsp = get("/book/search/Beckwith")

        then: "The list is returned with only one instance"
        rsp.json.totalCount == 1
        rsp.json.results.first().author == 'Burt Beckwith'
    }
}
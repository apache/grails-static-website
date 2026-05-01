package demo

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.testing.mixin.integration.Integration
import spock.lang.Shared
import spock.lang.Specification

@Integration
class MultipleDataSourceSpec extends Specification {

    @Shared
    RestBuilder rest = new RestBuilder()

    private RestResponse saveResource(String resource, String itemTitle, List<String> itemKeywords) {
        rest.post("http://localhost:${serverPort}/${resource}") {
            accept('application/json')
            contentType('application/json')
            json {
                title = itemTitle
                keywords = itemKeywords
            }
        }
    }

    private RestResponse deleteResource(String resource, String itemTitle) {
        rest.delete("http://localhost:${serverPort}/${resource}") {
            accept('application/json')
            contentType('application/json')
            json {
                title = itemTitle
            }
        }
    }

    private RestResponse fetchResource(String resource) {
        rest.get("http://localhost:${serverPort}/${resource}") {
            accept('application/json')
        }
    }

    private RestResponse resourceKeywords(String resource) {
        rest.get("http://localhost:${serverPort}/${resource}/keywords") {
            accept('application/json')
        }
    }

    def "Test Multi-Datasource support saving and retrieving books and movies"() {
        when:
        RestResponse resp = saveResource('book', 'Change Agent', ['dna', 'sci-fi'])

        then:
        resp.statusCode.value() == 201

        when:
        resp = saveResource('book', 'Influx', ['sci-fi'])

        then:
        resp.statusCode.value() == 201

        when:
        resp = saveResource('book', 'Kill Decision', ['drone', 'sci-fi'])

        then:
        resp.statusCode.value() == 201

        when:
        resp = saveResource('book', 'Freedom (TM)', ['sci-fi'])

        then:
        resp.statusCode.value() == 201

        when:
        resp = saveResource('book', 'Daemon', ['sci-fi'])

        then:
        resp.statusCode.value() == 201

        when:
        resp = saveResource('movie', 'Pirates of Silicon Valley', ['apple', 'microsoft', 'technology'])

        then:
        resp.statusCode.value() == 201

        when:
        resp = saveResource('movie', 'Inception', ['sci-fi'])

        then:
        resp.statusCode.value() == 201

        when:
        resp = fetchResource('book')

        then:
        resp.statusCode.value() == 200
        resp.json.collect { it.title }.sort() == ['Change Agent', 'Daemon', 'Freedom (TM)', 'Influx', 'Kill Decision']

        when:
        resp = fetchResource('movie')

        then:
        resp.statusCode.value() == 200
        resp.json.collect { it.title }.sort() == ['Inception', 'Pirates of Silicon Valley']

        when:
        resp = resourceKeywords('book')

        then:
        resp.statusCode.value() == 200
        resp.json.keywords == ['dna', 'drone', 'sci-fi']

        when:
        resp = resourceKeywords('movie')

        then:
        resp.statusCode.value() == 200
        resp.json.keywords == ['apple', 'microsoft', 'sci-fi', 'technology']


        when:
        resp = deleteResource('book', 'Change Agent')

        then:
        resp.statusCode.value() == 204

        when:
        resp = deleteResource('book', 'Influx')

        then:
        resp.statusCode.value() == 204

        when:
        resp = deleteResource('book', 'Kill Decision')

        then:
        resp.statusCode.value() == 204

        when:
        resp = deleteResource('book', 'Freedom (TM)')

        then:
        resp.statusCode.value() == 204

        when:
        resp = deleteResource('book', 'Daemon')

        then:
        resp.statusCode.value() == 204

        when:
        resp = deleteResource('movie', 'Pirates of Silicon Valley')

        then:
        resp.statusCode.value() == 204

        when:
        resp = deleteResource('movie', 'Inception')

        then:
        resp.statusCode.value() == 204
    }
}

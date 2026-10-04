package example

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import spock.lang.Specification

@Integration
@Rollback
class ItunesClientIntegrationSpec extends Specification {

    static MockWebServer mockWebServer = new MockWebServer()

    static {
        mockWebServer.start()
    }

    @Autowired
    ItunesClient itunesClient

    @DynamicPropertySource
    static void itunesBaseUrl(DynamicPropertyRegistry registry) {
        String baseUrl = mockWebServer.url('/').toString()
        if (baseUrl.endsWith('/')) {
            baseUrl = baseUrl[0..-2]
        }
        registry.add('itunes.base-url', { baseUrl })
    }

    void cleanupSpec() {
        mockWebServer.shutdown()
    }

    void 'declarative ItunesClient HTTP service is registered as a Spring bean'() {
        expect:
        itunesClient != null
        itunesClient instanceof ItunesClient
    }

    void 'search binds the term query parameter and deserializes albums'() {
        given:
        String searchTerm = 'U2 & Friends'
        mockWebServer.enqueue(new MockResponse()
                .setHeader('Content-Type', 'text/javascript; charset=utf-8')
                .setBody('''{"resultCount":1,"results":[{"artistName":"U2","collectionName":"The Joshua Tree","collectionViewUrl":"https://example.com/album"}]}'''))

        when:
        SearchResult result = itunesClient.search(searchTerm)

        then:
        result.resultCount == 1
        result.results.size() == 1
        result.results[0].artistName == 'U2'
        result.results[0].collectionName == 'The Joshua Tree'
        result.results[0].collectionViewUrl == 'https://example.com/album'

        and:
        RecordedRequest request = mockWebServer.takeRequest()
        request.method == 'GET'
        request.path.contains('/search?')
        request.requestUrl.queryParameter('term') == searchTerm
    }
}

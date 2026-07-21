package example

import spock.lang.Specification

class ItunesSearchServiceSpec extends Specification {

    ItunesSearchService service = new ItunesSearchService()

    void 'searchAlbums delegates to the ItunesClient HTTP service'() {
        given:
        def client = Mock(ItunesClient)
        service.itunesClient = client
        def albums = [new Album(artistName: 'U2', collectionName: 'The Joshua Tree')]
        client.search('U2') >> new SearchResult(resultCount: 1, results: albums)

        expect:
        service.searchAlbums('U2') == albums
    }

    void 'searchAlbums returns an empty list for blank terms'() {
        expect:
        service.searchAlbums(null).isEmpty()
        service.searchAlbums('   ').isEmpty()
    }
}

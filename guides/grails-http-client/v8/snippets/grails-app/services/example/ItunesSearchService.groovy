package example

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
class ItunesSearchService {

    @Autowired
    ItunesClient itunesClient

    List<Album> searchAlbums(String searchTerm) {
        if (!searchTerm?.trim()) {
            return []
        }
        SearchResult searchResult = itunesClient.search(searchTerm.trim())
        searchResult?.results ?: []
    }
}

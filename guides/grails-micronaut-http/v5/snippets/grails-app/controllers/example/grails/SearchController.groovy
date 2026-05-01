package example.grails

import groovy.transform.CompileStatic

@CompileStatic
class SearchController {

    ItunesSearchService itunesSearchService

    def searchWithApi(String searchTerm) {
        if(searchTerm) {
            List<Album> albums = itunesSearchService.searchWithApi(searchTerm)
            respond([searchTerm: searchTerm, albums: albums])
        }
    }

    // tag::searchWithDeclarativeClientController[]
    def searchWithDeclarativeClient(String searchTerm) {
        if(searchTerm) {
            List<Album> albums = itunesSearchService.searchWithDeclarativeClient(searchTerm)
            respond([searchTerm: searchTerm, albums: albums])
        }
    }
    // end::searchWithDeclarativeClientController[]
}

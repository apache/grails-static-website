package example

import groovy.transform.CompileStatic

@CompileStatic
class SearchController {

    static responseFormats = ['json']
    static allowedMethods = [index: 'GET']

    ItunesSearchService itunesSearchService

    def index(String q) {
        if (!q?.trim()) {
            respond([searchTerm: q, albums: []])
            return
        }
        List<Album> albums = itunesSearchService.searchAlbums(q)
        respond([searchTerm: q.trim(), albums: albums])
    }
}

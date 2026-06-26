package example

import groovy.transform.CompileStatic

@CompileStatic
class SearchResult {
    int resultCount
    List<Album> results = []
}

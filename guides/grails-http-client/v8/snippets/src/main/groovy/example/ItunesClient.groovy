package example

import groovy.transform.CompileStatic
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@CompileStatic
@HttpExchange(url = 'https://itunes.apple.com')
interface ItunesClient {

    @GetExchange('/search?limit=25&media=music&entity=album&term={term}')
    SearchResult search(String term)
}

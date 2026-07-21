package example

import groovy.transform.CompileStatic
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@CompileStatic
@HttpExchange
interface ItunesClient {

    @GetExchange('/search?limit=25&media=music&entity=album&term={term}')
    SearchResult search(@PathVariable('term') String term)
}

package example.grails

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client("https://itunes.apple.com/") // <1>
interface ItunesClient {

    @Get("/search?limit=25&media=music&entity=album&term={term}") // <2>
    SearchResult search(String term)
}

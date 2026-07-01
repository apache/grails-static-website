package demo

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class MovieService {

    @Transactional
    Movie addMovie(String title, List<String> keywords) {
        Movie movie = new Movie(title: title)
        if ( keywords ) {
            for ( String keyword : keywords ) {
                movie.addToKeywords(new Keyword(name: keyword))
            }
        }
        if ( !movie.save() ) {
            log.error 'Unable to save movie'
        }
        movie
    }



}
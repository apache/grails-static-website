package demo

import grails.gorm.services.Join
import grails.gorm.services.Service
import groovy.transform.CompileStatic

@CompileStatic
@Service(Movie)
interface MovieDataService {

    void deleteByTitle(String title)

    @Join('keywords') // <1>
    List<Movie> findAll()
}
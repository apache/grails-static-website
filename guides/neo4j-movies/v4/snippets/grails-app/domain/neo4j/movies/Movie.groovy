package neo4j.movies

import grails.compiler.GrailsCompileStatic

/**
 * Models a movie node in the graph database
 */
@GrailsCompileStatic
class Movie {
    String title
    String tagline
    int released

    static hasMany = [cast: CastMember]

    static constraints = {
        released min:1900
        title blank:false
    }
}

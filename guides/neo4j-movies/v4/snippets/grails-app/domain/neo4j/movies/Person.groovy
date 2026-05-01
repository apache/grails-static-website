package neo4j.movies

import grails.compiler.GrailsCompileStatic

/**
 * Models a Person node in the graph database
 */
@GrailsCompileStatic
class Person {
    String name
    int born

    static hasMany = [appearances: CastMember]

    static constraints = {
        name blank:false
        born min:1900
    }
}

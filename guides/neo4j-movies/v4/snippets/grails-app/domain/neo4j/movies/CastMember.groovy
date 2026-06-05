package neo4j.movies

import grails.neo4j.Relationship
import groovy.transform.CompileStatic

/**
 * Models a relationship between a Person and a Movie
 */
@CompileStatic
class CastMember implements Relationship<Person, Movie> { // <1>

    List<String> roles = [] // <2>

    CastMember() {
        type = RoleType.ACTED_IN // <3>
    }

    static enum RoleType { // <4>
        ACTED_IN, DIRECTED
    }
}

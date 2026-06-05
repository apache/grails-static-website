package org.grails.guides

class Model {

    String name

    static belongsTo = [ make: Make ]

    static constraints = {
    }

    String toString() {
        name
    }
}

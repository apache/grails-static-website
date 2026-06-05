package example

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Manufacturer {
    String name

    static constraints = {
        name blank: false
    }
}

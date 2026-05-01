package demo

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@GrailsCompileStatic
@EqualsAndHashCode(includes=['name', 'make', 'model'])
@ToString(includes=['name', 'make', 'model'], includeNames=true, includePackage=false)
class Vehicle {
    String name
    Make make
    Model model

    static belongsTo = [driver: Driver]

    static mapping = {
        name nullable: false
    }

    static constraints = {
    }
}

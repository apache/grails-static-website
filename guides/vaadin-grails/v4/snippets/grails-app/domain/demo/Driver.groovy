package demo

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@GrailsCompileStatic
@EqualsAndHashCode(includes=['name'])
@ToString(includes=['name'], includeNames=true, includePackage=false)
class Driver extends User {

    String name

    static hasMany = [ vehicles: Vehicle ]

    static constraints = {
        vehicles nullable: true
    }
}

package demo

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.Resource

@GrailsCompileStatic
@Secured(['ROLE_DRIVER'])
@Resource(uri = '/api/driver')
class Driver extends User {

    String name

    static hasMany = [ vehicles: Vehicle ]

    static constraints = {
        vehicles nullable: true
    }
}

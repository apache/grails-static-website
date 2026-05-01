package demo

import grails.rest.Resource

@Resource(uri = '/driver')
class Driver {

    String name

    static hasMany = [ vehicles: Vehicle ]

    static constraints = {
        vehicles nullable: true
    }
}

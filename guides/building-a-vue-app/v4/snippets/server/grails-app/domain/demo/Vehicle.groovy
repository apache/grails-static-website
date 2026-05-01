package demo

import grails.rest.Resource

@Resource(uri = '/vehicle')
class Vehicle {

    String name

    Make make
    Model model

    static belongsTo = [driver: Driver]
}

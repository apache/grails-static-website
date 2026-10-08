package demo

import grails.gorm.MultiTenant

class Vehicle implements MultiTenant<Vehicle> { // <1>
    String model
    Integer year

    static hasMany = [engines: Engine]
    static mapping = {
        year column: '`year`'
    }
    static constraints = {
        model nullable: false, blank: false
        year nullable: false, min: 1980
    }
}

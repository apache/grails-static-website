package example

import grails.gorm.MultiTenant

class Vehicle implements MultiTenant<Vehicle> { // <1>
    String model
    Integer year

    static hasMany = [engines: Engine]
    static constraints = {
        model blank:false
        year min:1980
    }
}

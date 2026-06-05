package example

import grails.gorm.MultiTenant

class Vehicle implements MultiTenant<Vehicle> { // <1>
    String model
    Integer year
    String manufacturer // <2>

    static hasMany = [engines: Engine]
    static constraints = {
        model blank:false
        year min:1980
    }

    static mapping = {
        tenantId name:'manufacturer'  // <2>
    }
}

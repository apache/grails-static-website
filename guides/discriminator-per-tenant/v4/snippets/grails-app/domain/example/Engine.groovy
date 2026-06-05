package example

import grails.gorm.MultiTenant

class Engine implements MultiTenant<Engine> { // <1>
    Integer cylinders
    String manufacturer  // <2>

    static constraints = {
        cylinders nullable: false
    }

    static mapping = {
        tenantId name:'manufacturer'  // <2>
    }
}

package example

import grails.gorm.MultiTenant

class Engine implements MultiTenant<Engine> { // <1>
    Integer cylinders

    static constraints = {
        cylinders nullable: false
    }
}

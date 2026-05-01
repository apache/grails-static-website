package demo.domain

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity
import groovy.transform.ToString

@ToString
@Entity
class Vehicle implements GormEntity<Vehicle> {
    String name
    Integer year
    static belongsTo = [manufacturer: Manufacturer]

    static constraints = {
        name nullable: false, blank: false
    }
}

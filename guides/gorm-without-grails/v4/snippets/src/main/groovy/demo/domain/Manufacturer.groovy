package demo.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

@ToString
@Entity
class Manufacturer implements GormEntity<Manufacturer> {

    String name

    static hasMany = [vehicles: Vehicle]

    static constraints = {
        name blank: false
    }
}

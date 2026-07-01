package demo

import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileStatic

@CompileStatic
@ReadOnly
class VehicleService {

    def save(final Vehicle vehicle) {
        vehicle.save(failOnError: true)
    }

    @ReadOnly
    List<Vehicle> listAll(boolean lazyFetch = true) {
        if ( !lazyFetch ) {
            return Vehicle.where {}
                    .join('make')
                    .join('model')
                    .join('driver')
                    .list()
        }
        Vehicle.where { }.list()
    }
}

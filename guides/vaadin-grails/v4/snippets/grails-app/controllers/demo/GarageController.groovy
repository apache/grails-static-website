package demo

import grails.converters.JSON
import groovy.transform.CompileStatic

@CompileStatic
class GarageController {

    VehicleService vehicleService //<1>

    def index() { //<2>
        final List<Vehicle> vehicleList = vehicleService.listAll()

        render vehicleList as JSON
    }
}

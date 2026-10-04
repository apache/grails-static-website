package example

import groovy.transform.CompileStatic

@CompileStatic
class BootStrap {

    MakeService makeService
    ModelService modelService
    VehicleService vehicleService

    def init = {
        Make nissan = makeService.save('Nissan')
        Make ford = makeService.save('Ford')

        Model titan = modelService.save('Titan', nissan)
        Model leaf = modelService.save('Leaf', nissan)
        Model windstar = modelService.save('Windstar', ford)

        vehicleService.save('Pickup', nissan, titan, 2012)
        vehicleService.save('Economy', nissan, leaf, 2014)
        vehicleService.save('Minivan', ford, windstar, 1990)
    }

    def destroy = {
    }
}

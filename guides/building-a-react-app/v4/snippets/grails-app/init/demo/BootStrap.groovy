package demo

import demo.Driver
import demo.Make
import demo.Model
import demo.Vehicle

class BootStrap {

    def init = { servletContext ->
        log.info "Loading database..."
        def driver1 = new Driver(name: "Susan").save()
        def driver2 = new Driver(name: "Pedro").save()

        def nissan = new Make(name: "Nissan").save()
        def ford = new Make(name: "Ford").save()

        def titan = new Model(name: "Titan").save()
        def leaf = new Model(name: "Leaf").save()
        def windstar = new Model(name: "Windstar").save()

        new Vehicle(name: "Pickup", driver: driver1, make: nissan, model: titan).save()
        new Vehicle(name: "Economy", driver: driver1, make: nissan, model: leaf).save()
        new Vehicle(name: "Minivan", driver: driver2, make: ford, model: windstar).save()

    }
    def destroy = {
    }
}

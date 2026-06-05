package example

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration

@Integration
class TenantSelectionFuncSpec extends GebSpec {

    def "it is possible to change tenants and get different lists of vehicles"() {

        when:
        go '/'

        then:
        at ManufacturersPage

        when:
        selectAudi()

        then:
        at VehiclesPage

        when:
        newVehicle()

        then:
        at NewVehiclePage

        when:
        newVehicle('A5', 2000)

        then:
        at ShowVehiclePage

        when:
        vehicleList()

        then:
        at VehiclesPage
        numberOfVehicles() == 1

        when:
        newVehicle()

        then:
        at NewVehiclePage

        when:
        newVehicle('A3', 2001)

        then:
        at ShowVehiclePage

        when:
        vehicleList()

        then:
        at VehiclesPage
        numberOfVehicles() == 2

        when:
        go '/'

        then:
        at ManufacturersPage

        when:
        selectFord()

        then:
        at VehiclesPage

        when:
        newVehicle()

        then:
        at NewVehiclePage

        when:
        newVehicle('KA', 1996)

        then:
        at ShowVehiclePage

        when:
        vehicleList()

        then:
        at VehiclesPage
        numberOfVehicles() == 1
    }

}

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
        ManufacturersPage page = browser.page(ManufacturersPage)
        page.selectAudi()

        then:
        at VehiclesPage

        when:
        VehiclesPage vehiclesPage = browser.page(VehiclesPage)
        vehiclesPage.newVehicle()

        then:
        at NewVehiclePage

        when:
        NewVehiclePage newVehiclePage = browser.page(NewVehiclePage)
        newVehiclePage.newVehicle('A5', 2000)

        then:
        at ShowVehiclePage

        when:
        ShowVehiclePage showVehiclePage = browser.page(ShowVehiclePage)
        showVehiclePage.vehicleList()

        then:
        at VehiclesPage

        when:
        vehiclesPage = browser.page(VehiclesPage)

        then:
        vehiclesPage.numberOfVehicles() == 1

        when:
        vehiclesPage.newVehicle()

        then:
        at NewVehiclePage

        when:
        newVehiclePage = browser.page(NewVehiclePage)
        newVehiclePage.newVehicle('A3', 2001)

        then:
        at ShowVehiclePage

        when:
        showVehiclePage = browser.page(ShowVehiclePage)
        showVehiclePage.vehicleList()

        then:
        at VehiclesPage

        when:
        vehiclesPage = browser.page(VehiclesPage)

        then:
        vehiclesPage.numberOfVehicles() == 2

        when:
        go '/'

        then:
        at ManufacturersPage

        when:
        ManufacturersPage manufacturersPage = browser.page(ManufacturersPage)
        manufacturersPage.selectFord()

        then:
        at VehiclesPage

        when:
        vehiclesPage = browser.page(VehiclesPage)
        vehiclesPage.newVehicle()

        then:
        at NewVehiclePage

        when:
        newVehiclePage = browser.page(NewVehiclePage)
        newVehiclePage.newVehicle('KA', 1996)

        then:
        at ShowVehiclePage

        when:
        showVehiclePage = browser.page(ShowVehiclePage)
        showVehiclePage.vehicleList()

        then:
        at VehiclesPage

        when:
        vehiclesPage = browser.page(VehiclesPage)

        then:
        vehiclesPage.numberOfVehicles() == 1
    }

}

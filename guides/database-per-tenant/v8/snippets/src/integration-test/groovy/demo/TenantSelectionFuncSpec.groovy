package demo

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf

@Integration
@IgnoreIf({ !System.getenv('CI') && !isDockerAvailable() })
class TenantSelectionFuncSpec extends ContainerGebSpec {

    private static boolean isDockerAvailable() {
        try {
            def process = ['docker', 'info'].execute()
            process.waitFor()
            return process.exitValue() == 0
        } catch (Exception ignored) {
            return false
        }
    }

    def "it is possible to change tenants and get different lists of vehicles"() {

        when:
        to ManufacturersPage

        then:
        at ManufacturersPage

        when:
        page.selectAudi()

        then:
        at VehiclesPage

        when:
        page.newVehicle()

        then:
        at NewVehiclePage

        when:
        page.newVehicle('A5', 2000)

        then:
        at ShowVehiclePage

        when:
        page.vehicleList()

        then:
        at VehiclesPage
        page.numberOfVehicles() == 1

        when:
        page.newVehicle()

        then:
        at NewVehiclePage

        when:
        page.newVehicle('A3', 2001)

        then:
        at ShowVehiclePage

        when:
        page.vehicleList()

        then:
        at VehiclesPage
        page.numberOfVehicles() == 2

        when:
        to ManufacturersPage

        then:
        at ManufacturersPage

        when:
        page.selectFord()

        then:
        at VehiclesPage

        when:
        page.newVehicle()

        then:
        at NewVehiclePage

        when:
        page.newVehicle('KA', 1996)

        then:
        at ShowVehiclePage

        when:
        page.vehicleList()

        then:
        at VehiclesPage
        page.numberOfVehicles() == 1
    }

}

package demo

import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class TenantIsolationIntegrationSpec extends Specification {

    VehicleService vehicleService

    void 'vehicles created for one tenant are not visible to another'() {
        when: 'create a vehicle in the audi tenant'
        Long audiId = Tenants.withId('audi') {
            vehicleService.save('A5', 2011).id
        }

        then:
        Tenants.withId('audi') {
            vehicleService.count() == 1
            vehicleService.find(audiId)?.model == 'A5'
        }

        when: 'switch to the ford tenant'
        then: 'audi data is not visible'
        Tenants.withId('ford') {
            vehicleService.count() == 0
            vehicleService.find(audiId) == null
        }

        when: 'create a ford vehicle'
        Long fordId = Tenants.withId('ford') {
            vehicleService.save('KA', 1996).id
        }

        then:
        Tenants.withId('ford') {
            vehicleService.count() == 1
            vehicleService.find(fordId)?.model == 'KA'
        }

        cleanup:
        Tenants.withId('audi') {
            if (audiId) {
                vehicleService.delete(audiId)
            }
        }
        Tenants.withId('ford') {
            if (fordId) {
                vehicleService.delete(fordId)
            }
        }
    }
}

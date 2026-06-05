package example

import grails.test.hibernate.HibernateSpec
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.grails.datastore.mapping.multitenancy.resolvers.SystemPropertyTenantResolver
import spock.lang.Stepwise

// tag::class[]
@Stepwise
class VehicleControllerSpec extends HibernateSpec implements ControllerUnitTest<VehicleController> {
// end::class[]

    // tag::config[]
    @Override
    Map getConfiguration() {
        [(Settings.SETTING_MULTI_TENANT_RESOLVER_CLASS): SystemPropertyTenantResolver]
    }
    // end::config[]

    // tag::setup[]
    VehicleService vehicleService // <1>
    def setup() {
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, 'audi') // <2>
        vehicleService = hibernateDatastore.getService(VehicleService) // <3>
        controller.vehicleService = vehicleService // <4>
    }
    // end::setup[]

    // tag::cleanup[]
    def cleanup() {
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, '')
    }
    // end::cleanup[]

    // tag::index[]
    void 'Test the index action returns the correct model'() {

        when: 'The index action is executed'
        controller.index()

        then: 'The model is correct'
        !model.vehicleList
        model.vehicleCount == 0
    }
    // end::index[]

    // tag::noTenant[]
    void 'Test the index action with no tenant id'() {
        when: 'there is no tenant id'
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, '')
        controller.index()

        then:
        thrown(TenantNotFoundException)
    }
    // end::noTenant[]

    void 'Test the create action returns the correct model'() {
        when: 'The create action is executed'
        controller.create()

        then: 'The model is correctly created'
        model.vehicle != null
    }

    // tag::save[]
    void 'Test the save action correctly persists an instance'() {

        when: 'The save action is executed with an invalid instance'
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.save('', 1900)

        then: 'The create view is rendered again with the correct model'
        model.vehicle != null
        view == 'create'

        when: 'The save action is executed with a valid instance'
        response.reset()
        controller.save('A5', 2011)

        then: 'A redirect is issued to the show action'
        response.redirectedUrl == '/vehicles/1'
        controller.flash.message != null
        vehicleService.count() == 1
    }
    // end::save[]

    void 'Test that the show action returns 404 for an invalid id'() {
        when: 'The show action is executed with a null domain'
        controller.show(null)

        then: 'A 404 error is returned'
        response.status == 404
    }

    void 'Test the update action performs an update on a valid domain instance'() {
        when: 'Update is called for a domain instance that doesn\'t exist'
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'PUT'
        controller.update(999, 'A5', 2011)

        then: 'A 404 error is returned'
        response.redirectedUrl == '/vehicles'
        flash.message != null

        when: 'An invalid domain instance is passed to the update action'
        response.reset()
        controller.update(1, 'A5', 1900)

        then: 'The edit view is rendered again with the invalid instance'
        view == 'edit'
        model.vehicle instanceof Vehicle

        when: 'A valid domain instance is passed to the update action'
        response.reset()
        controller.update(1, 'A5', 2012)

        then: 'A redirect is issued to the show action'
        response.redirectedUrl == '/vehicles/1'
        flash.message != null
    }

    void 'Test that the delete action deletes an instance if it exists'() {
        when: 'The delete action is called for a null instance'
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'DELETE'
        controller.delete(null)

        then: 'A 404 is returned'
        response.redirectedUrl == '/vehicles'
        flash.message != null
        vehicleService.count() == 1

        when: 'A domain instance is created'
        response.reset()
        controller.delete(1)

        then: 'The instance is deleted'
        vehicleService.count() == 0
        response.redirectedUrl == '/vehicles'
        flash.message != null
    }
}

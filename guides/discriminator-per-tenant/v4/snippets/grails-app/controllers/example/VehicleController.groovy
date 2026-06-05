package example

// tag::class[]
import static org.springframework.http.HttpStatus.NOT_FOUND
import grails.gorm.multitenancy.CurrentTenant
import grails.validation.ValidationException
import groovy.transform.CompileStatic

@CompileStatic
class VehicleController  {

    static allowedMethods = [save: 'POST', update: 'PUT', delete: 'DELETE']

    VehicleService vehicleService
// end::class[]

    // tag::index[]
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond vehicleService.list(params), model: [vehicleCount: vehicleService.count()]
    }
    // end::index[]

    // tag::show[]
    def show(Long id) {
        Vehicle vehicle = id ? vehicleService.find(id) : null
        respond vehicle
    }
    // end::show[]

    // tag::create[]
    @CurrentTenant
    def create() {
        respond new Vehicle(params)
    }

    @CurrentTenant
    def edit(Long id) {
        show id
    }
    // end::create[]

    // tag::save[]
    def save(String model, Integer year) {
        try {
            Vehicle vehicle = vehicleService.save(model, year)
            flash.message = 'Vehicle created'
            redirect vehicle
        } catch (ValidationException e) {
            respond e.errors, view: 'create'
        }
    }
    // end::save[]

    // tag::update[]
    def update(Long id, String model, Integer year) {
        try {
            Vehicle vehicle = vehicleService.update(id, model, year)
            if (vehicle == null) {
                notFound()
            } else if ( vehicle.hasErrors() ) {
                redirect action: 'edit', params: [id: id]
            }
            else {
                flash.message = 'Vehicle updated'
                redirect vehicle
            }
        } catch (ValidationException e) {
            respond e.errors, view: 'edit'
        }
    }

    protected void notFound() {
        flash.message = 'Vehicle not found'
        redirect uri: '/vehicles', status: NOT_FOUND
    }
    // end::update[]

    // tag::delete[]
    def delete(Long id) {
        Vehicle vehicle = vehicleService.delete(id)
        if (vehicle == null) {
            notFound()
        }
        else {
            flash.message = 'Vehicle Deleted'
            redirect action: 'index', method: 'GET'
        }
    }
    // end::delete[]
}

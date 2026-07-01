package example

// tag::class[]
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Service(Vehicle) // <1>
@CurrentTenant // <2>
@CompileStatic
abstract class VehicleService {
// end::class[]

    // tag::queries[]
    abstract List<Vehicle> list(Map args ) // <1>

    abstract Integer count() // <2>

    abstract Vehicle find(Serializable id) // <3>

    // end::queries[]

    // tag::save[]
    abstract Vehicle save(String model,
                            Integer year)
    // end::save[]

    // tag::update[]
    @Transactional
    Vehicle update( Serializable id, // <5>
                    String model,
                    Integer year) {
        Vehicle vehicle = find(id)
        if (vehicle != null) {
            vehicle.model = model
            vehicle.year = year
            vehicle.save(failOnError:true)
        }
        vehicle
    }
    // end::update[]

    // tag::delete[]
    abstract Vehicle delete(Serializable id)
    // end::delete[]
}

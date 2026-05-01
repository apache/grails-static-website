package example

// tag::class[]
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.services.Join
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Service(Vehicle) // <1>
@CurrentTenant // <2>
@CompileStatic
abstract class VehicleService {
// end::class[]

    // tag::queries[]
    @Join('engines') // <1>
    abstract List<Vehicle> list(Map args ) // <2>

    abstract Integer count() // <3>

    @Join('engines')
    abstract Vehicle find(Serializable id) // <4>

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

// tag::packageDeclaration[]
package example

// end::packageDeclaration[]
// tag::importStatements[]
import org.grails.datastore.mapping.multitenancy.web.SessionTenantResolver
import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileStatic

// end::importStatements[]

// tag::classDeclaration[]
@CompileStatic
class ManufacturerController {
// end::classDeclaration[]
    ManufacturerService manufacturerService

    // tag::index[]
    @ReadOnly
    def index() {
        render view: '/index', model: [manufacturers: manufacturerService.findAll()]
    }
    // end::index[]

    // tag::select[]
    @ReadOnly
    def select(String id) {
        Manufacturer m = manufacturerService.findByName(id) // <1>
        if ( m ) {
            session.setAttribute(SessionTenantResolver.ATTRIBUTE, m.name.toLowerCase()) // <2>
            redirect controller: 'vehicle' // <3>
        }
        else {
            render status: 404
        }
    }
    // end::select[]

// tag::closeBracket[]
}
// end::closeBracket[]

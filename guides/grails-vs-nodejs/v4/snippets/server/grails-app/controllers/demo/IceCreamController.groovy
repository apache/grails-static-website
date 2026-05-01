//tag::package[]
package demo
//end::package[]

//tag::import[]
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

//end::import[]

//tag::class[]
@CompileStatic
@Secured(['ROLE_USER']) //<1>
class IceCreamController {
    static responseFormats = ['json']

    static allowedMethods = [index: 'GET', save: 'POST', delete: 'DELETE',]
//end::class[]

//tag::dependency[]
    IceCreamService iceCreamService

    UserIceCreamService userIceCreamService

    SpringSecurityService springSecurityService
    
//end::dependency[]

//tag::index[]
    def index(Integer max) {
        String username = loggedUsername()
        if ( !username ) {
            render status: 404
            return
        }
        params.max = Math.min(max ?: 10, 100) //<2>

        List<IceCream> iceCreams = userIceCreamService.findAllIceCreamsByUsername(username)
        [iceCreams: iceCreams] //<3>
    }
//end::index[]

//tag::save[]
    def save(String flavor) {
        String username = loggedUsername()
        if ( !username ) {
            render status: 404
            return
        }
        def id = iceCreamService.addIceCreamToUser(username, flavor)?.id
        render id ?: [status: 500]
    }
//end::save[]

//tag::delete[]
    def delete(Long id) {
        String username = loggedUsername()
        if ( !username ) {
            render status: 404
            return
        }
        respond iceCreamService.removeIceCreamFromUser(username, id) ?: [status: 500]
    }
//end::delete[]

//tag::loggedUsername[]
    @CompileDynamic
    protected String loggedUsername() {
        springSecurityService.principal?.username as String
    }
//end::loggedUsername[]    
}
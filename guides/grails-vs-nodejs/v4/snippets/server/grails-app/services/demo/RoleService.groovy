//tag::package[]
package demo

//end::package[]

//tag::import[]
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
//end::import[]

//tag::class[]
@Slf4j
@Transactional
@CompileStatic
class RoleService {
//end::class[]

//tag::saveRole[]
    Role saveRole(String authority, boolean flush = false) {
        Role r = new Role(authority: authority)
        if ( !r.save(flush: flush) ) {
            log.error 'Failure while saving role {}', r.errors.toString()
        }
        r
    }
//end::saveRole[]    

//tag::findByRoleName[]    
    Role findByRoleName(String roleName) {
        Role.where { authority == roleName }.get()
    }
//end::findByRoleName[]    

//tag::count[]    
    @ReadOnly
    int count() {
        Role.count() as int
    }
//end::count[]        
}
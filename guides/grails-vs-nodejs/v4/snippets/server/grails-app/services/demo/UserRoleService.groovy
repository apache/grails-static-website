//tag::package[]
package demo

//end::package[]
//tag::import[]
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
//end::import[]

//tag::class[]
@Slf4j
@Transactional
@CompileStatic
class UserRoleService {
//end::class[]

//tag::saveUserRole[]
    UserRole saveUserRole(User user, Role role, boolean flush = false) {
        UserRole ur = new UserRole(user: user, role: role)
        if ( !ur.save(flush: flush) ) {
            log.error 'Failure while saving user {}', ur.errors.toString()
        }
        ur
    }
//end::saveUserRole[]    
}
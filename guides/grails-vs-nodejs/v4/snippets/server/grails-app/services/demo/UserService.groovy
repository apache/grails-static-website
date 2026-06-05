//tag::package[]
package demo

//end::package[]
//tag::import[]
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
//end::import[]

//tag::class[]
@Slf4j
@Transactional
class UserService {
//end::class[]

//tag::createUser[]
    User createUser(String username, String password, boolean flush = false) {
        User user = new User(username: username, password: password)
        if ( !user.save(flush: flush) ) {
            log.error 'Unable to save user {}', user.errors.toString()
        }
        user
    }
//end::createUser[]

//tag::existsUserByUsername[]
    @ReadOnly
    boolean existsUserByUsername(String username) {
        findQueryByUsername(username).count()
    }
//end::existsUserByUsername[]

//tag::findQueryByUsername[]
    DetachedCriteria<User> findQueryByUsername(String name) {
        User.where { username == name }
    }
//end::findQueryByUsername[]

//tag::findByUsername[]
    @ReadOnly
    User findByUsername(String username) {
        findQueryByUsername(username).get()
    }
//end::findByUsername[]    
}

//tag::package[]
package demo
//end::package[]

//tag::import[]
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.validation.ObjectError

//end::import[]

//tag::class[]
@Slf4j
@Transactional
@CompileStatic
class UserIceCreamService {
//end::class[]

//tag::create[]
    UserIceCream create(User user, IceCream iceCream, boolean flush = false) { //<1>
        UserIceCream instance = new UserIceCream(user: user, iceCream: iceCream)
        if ( !instance.save(flush: flush) ) { //<2>
            instance.errors.allErrors.each { ObjectError error ->
                log.error(error.toString())
            }
        }
        instance
    }
//end::create[]    

//tag::findAllIceCreamsByUsername[]    
    @ReadOnly
    List<IceCream> findAllIceCreamsByUsername(String loggedUsername) {
        UserIceCream.where {
            user.username == loggedUsername
        }.list()*.iceCream as List<IceCream>
    }
//end::findAllIceCreamsByUsername[]        
}
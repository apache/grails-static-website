//tag::package[]
package demo

//end::package[]
//tag::import[]
import grails.compiler.GrailsCompileStatic
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.validation.ObjectError
//end::import[]

//tag::class[]
@Slf4j
@CompileStatic
@Transactional
class IceCreamService {
//end::class[]

//tag::dependency[]
    UserIceCreamService userIceCreamService

    UserService userService
//end::dependency[]

//tag::saveIcream[]
    IceCream saveIcream(String flavor, boolean flush = false) {
        IceCream iceCream = new IceCream(flavor: flavor)
        if ( !iceCream.save(flush: flush) ) {
            log.error 'Failure while saving icream {}', iceCream.errors.toString()
        }
        iceCream
    }
//end::saveIcream[]

//tag::count[]
    @ReadOnly
    int count() {
        IceCream.count() as int
    }
//end::count[]

//tag::findQueryByFlavor[]
    DetachedCriteria<IceCream> findQueryByFlavor(String iceCreamFlavor) {
        IceCream.where { flavor == iceCreamFlavor }
    }
//end::findQueryByFlavor[]

//tag::addIceCreamToUser[]
    /**
     * @return null if an error occurs while saving the ice cream or the association between icream and user
     */
    @GrailsCompileStatic
    IceCream addIceCreamToUser(String username, String iceCreamFlavor, boolean flush = false) {
        User user = userService.findByUsername(username)
        if ( !user ) {
            log.error 'User {} does not exist', username
            return null
        }
        IceCream iceCream = findQueryByFlavor(iceCreamFlavor).get() ?: new IceCream(flavor: iceCreamFlavor)

        if(!iceCream.save(flush: flush)) {
            iceCream.errors.allErrors.each { ObjectError error ->
                log.error(error.toString())
            }
            return null
        }
        UserIceCream userIceCream = userIceCreamService.create(user, iceCream, flush)
        if ( userIceCream.hasErrors() ) {
            return null
        }
        iceCream
    }
//end::addIceCreamToUser[]    

//tag::removeIceCreamFromUser[]    
    Boolean removeIceCreamFromUser(String loggedUsername, Long id) {
        IceCream iceCreamEntity = IceCream.load(id)
        User loggedUser = User.load(loggedUsername)
        UserIceCream.where {
            user == loggedUser && iceCream == iceCreamEntity
        }.deleteAll()
    }
//end::removeIceCreamFromUser[]    
}

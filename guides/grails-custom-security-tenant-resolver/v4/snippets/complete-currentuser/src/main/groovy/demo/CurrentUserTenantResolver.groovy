package demo

import grails.gorm.DetachedCriteria
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.multitenancy.AllTenantsResolver
import org.grails.datastore.mapping.multitenancy.TenantResolver
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.userdetails.UserDetails

@CompileStatic
class CurrentUserTenantResolver implements AllTenantsResolver { // <1>

    @Autowired
    @Lazy
    SpringSecurityService springSecurityService

    @Override
    Serializable resolveTenantIdentifier() throws TenantNotFoundException {
        String username = loggedUsername()
        if ( username ) {
            return username
        }
        throw new TenantNotFoundException("Tenant could not be resolved from Spring Security Principal")
    }

    String loggedUsername() {
        if ( springSecurityService.principal instanceof String ) {
            return springSecurityService.principal
        }
        if (springSecurityService.principal instanceof UserDetails) {
            return ((UserDetails) springSecurityService.principal).username
        }
        null
    }

    @Override
    Iterable<Serializable> resolveTenantIds() {
        User.withTransaction(readOnly: true) {
            new DetachedCriteria(User)
                    .distinct('username')
                    .list()  as Iterable<Serializable>
        }
    }
}

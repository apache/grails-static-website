package demo

import grails.gorm.DetachedCriteria
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.multitenancy.AllTenantsResolver
import org.grails.datastore.mapping.multitenancy.TenantResolver
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletWebRequest

import javax.servlet.http.HttpServletRequest

@CompileStatic
class CurrentUserByJwtTenantResolver implements AllTenantsResolver { // <1>

    public static final String HEADER_NAME = 'Authorization'
    public static final String HEADER_VALUE_PREFFIX = 'Bearer '

    String headerName = HEADER_NAME
    String headerValuePreffix = HEADER_VALUE_PREFFIX

    @Autowired
    TokenStorageService tokenStorageService

    @Override
    Serializable resolveTenantIdentifier() throws TenantNotFoundException {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes()
        if(requestAttributes instanceof ServletWebRequest) {

            HttpServletRequest httpServletRequest = ((ServletWebRequest) requestAttributes).getRequest()
            String token = httpServletRequest.getHeader(headerName.toLowerCase())
            if ( !token ) {
                throw new TenantNotFoundException("Tenant could not be resolved from HTTP Header: ${headerName}")
            }

            if (token.startsWith(headerValuePreffix)) {
                token = token.substring(headerValuePreffix.length())
            }
            UserDetails userDetails = tokenStorageService.loadUserByToken(token)
            String username = userDetails?.username
            if ( username ) {
                return username
            }
            throw new TenantNotFoundException("Tenant could not be resolved from HTTP Header: ${headerName}")
        }
        throw new TenantNotFoundException("Tenant could not be resolved outside a web request")
    }

    @Override
    Iterable<Serializable> resolveTenantIds() {
        new DetachedCriteria(User)
                .distinct('username')
                .list()
    }
}

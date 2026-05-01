package demo

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.testing.mixin.integration.Integration
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletWebRequest
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf( { System.getenv('TRAVIS') as boolean } )
@Integration
class CurrentUserByJwtTenantResolverSpec extends Specification {

    @Autowired
    CurrentUserByJwtTenantResolver currentUserTenantResolver

    TokenGenerator tokenGenerator


    void "Test HttpHeader resolver throws an exception outside a web request"() {
        when:
        currentUserTenantResolver.resolveTenantIdentifier()

        then:
        def e = thrown(TenantNotFoundException)
        e.message == "Tenant could not be resolved outside a web request"
    }


    void "Test not tenant id found"() {
        setup:
        def request = new MockHttpServletRequest("GET", "/foo")
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(request))

        when:
        currentUserTenantResolver.resolveTenantIdentifier()

        then:
        def e = thrown(TenantNotFoundException)
        e.message == "Tenant could not be resolved from HTTP Header: ${CurrentUserByJwtTenantResolver.HEADER_NAME}"

        cleanup:
        RequestContextHolder.setRequestAttributes(null)
    }

    void "Test HttpHeader value is the tenant id when a request is present"() {

        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/foo")
        def userDetails = Stub(UserDetails) {
            getUsername() >> 'vector'
            isAccountNonExpired() >> true
            isAccountNonLocked() >> true
            isCredentialsNonExpired() >> true
            isEnabled() >> true
        }
        AccessToken accessToken = tokenGenerator.generateAccessToken(userDetails, 3600)
        String jwt = accessToken.accessToken
        request.addHeader('Authorization', "Bearer $jwt")
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(request))


        when:
        def tenantId = currentUserTenantResolver.resolveTenantIdentifier()

        then:
        tenantId == "vector"

        cleanup:
        RequestContextHolder.setRequestAttributes(null)
    }
}


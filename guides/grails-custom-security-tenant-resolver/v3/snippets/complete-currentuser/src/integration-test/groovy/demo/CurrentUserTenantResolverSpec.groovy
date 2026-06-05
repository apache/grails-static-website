package demo

import grails.testing.mixin.integration.Integration
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

@Integration
class CurrentUserTenantResolverSpec extends Specification {

    UserDataService userDataService
    RoleDataService roleDataService
    UserRoleDataService userRoleDataService

    @Autowired
    CurrentUserTenantResolver currentUserTenantResolver

    void "Test Current User throws a TenantNotFoundException if not logged in"() {
        when:
        currentUserTenantResolver.resolveTenantIdentifier()

        then:
        def e = thrown(TenantNotFoundException)
        e.message == "Tenant could not be resolved from Spring Security Principal"
    }

    void "Test current logged in user is resolved "() {
        given:
        Role role = roleDataService.saveByAuthority('ROLE_USER')
        User user = userDataService.save('admin', 'admin')
        userRoleDataService.save(user, role)

        when:
        loginAs('admin', 'ROLE_USER')
        Serializable username = currentUserTenantResolver.resolveTenantIdentifier()

        then:
        username == user.username

        when: "verify AllTenantsResolver::resolveTenantIds"
        Iterable<Serializable> tenantIds
        demo.User.withNewSession {
            tenantIds = currentUserTenantResolver.resolveTenantIds()
        }

        then:
        tenantIds.toList().size() == 1
        tenantIds.toList().get(0) == 'admin'

        cleanup:
        userRoleDataService.delete(user, role)
        roleDataService.delete(role)
        userDataService.delete(user.id)
    }

    void loginAs(String username, String authority) {
        User user = userDataService.findByUsername(username)
        if ( user ) {
            // have to be authenticated as an admin to create ACLs
            List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(authority)
            SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(user.username,
                    user.password,
                    authorityList)
        }
    }


}

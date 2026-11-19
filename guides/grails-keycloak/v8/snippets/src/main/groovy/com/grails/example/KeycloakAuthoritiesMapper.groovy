package com.grails.example

import grails.core.GrailsApplication
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority

class KeycloakAuthoritiesMapper implements GrantedAuthoritiesMapper {

    @Autowired
    GrailsApplication grailsApplication

    private String clientId

    @PostConstruct
    void init() {
        clientId = grailsApplication.config.getProperty(
                'spring.security.oauth2.client.registration.keycloak.client-id'
        )
    }

    @Override
    Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {

        def mappedAuthorities = new HashSet<GrantedAuthority>()

        authorities.each { authority ->
            if (authority instanceof OidcUserAuthority) {
                addKeycloakRoles(
                        mappedAuthorities,
                        authority.idToken.claims
                )
            } else {
                mappedAuthorities.add(authority)
            }
        }

        return mappedAuthorities
    }

    private void addKeycloakRoles(
            Set<GrantedAuthority> authorities,
            Map<String, Object> claims) {

        def resourceAccess = claims['resource_access']

        if (!(resourceAccess instanceof Map)) {
            return
        }

        def clientAccess = resourceAccess[clientId]

        if (!(clientAccess instanceof Map)) {
            return
        }

        def roles = clientAccess['roles']

        if (!(roles instanceof Collection)) {
            return
        }

        roles.each { role ->
            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_${role.toString().toUpperCase()}"
                    )
            )
        }
    }
}

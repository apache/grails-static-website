package com.grails.example

import grails.config.Config
import grails.core.GrailsApplication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import spock.lang.Specification

import java.time.Instant

class KeycloakAuthoritiesMapperSpec extends Specification {

    private static final String CLIENT_ID_PROPERTY =
            'spring.security.oauth2.client.registration.keycloak.client-id'

    private KeycloakAuthoritiesMapper mapperFor(String clientId) {
        Config config = Mock(Config) {
            getProperty(CLIENT_ID_PROPERTY) >> clientId
        }
        GrailsApplication grailsApplication = Stub(GrailsApplication) {
            getConfig() >> config
        }
        def mapper = new KeycloakAuthoritiesMapper(grailsApplication: grailsApplication)
        mapper.init()
        mapper
    }

    private static OidcUserAuthority tokenWithClaims(Map<String, Object> claims) {
        new OidcUserAuthority(new OidcIdToken(
                'id-token-value',
                Instant.now(),
                Instant.now().plusSeconds(300),
                claims))
    }

    private static Set<String> roleAuthorities(Collection<? extends GrantedAuthority> authorities) {
        authorities.collect { it.authority }.findAll { it.startsWith('ROLE_') } as Set
    }

    void "maps client roles from resource_access to ROLE_ authorities"() {
        given: 'an ID token carrying client roles for example-client'
        def mapper = mapperFor('example-client')
        def authority = tokenWithClaims([
                resource_access: [
                        'example-client': [roles: ['USER', 'ADMIN']]
                ]
        ])

        when: 'the authorities are mapped'
        def mapped = mapper.mapAuthorities([authority])

        then: 'the client roles become Spring Security authorities'
        roleAuthorities(mapped) == ['ROLE_USER', 'ROLE_ADMIN'] as Set
    }

    void "ignores realm roles when no resource_access claim is present"() {
        given: 'an ID token carrying only realm roles'
        def mapper = mapperFor('example-client')
        def authority = tokenWithClaims([
                realm_access: [roles: ['USER', 'ADMIN']]
        ])

        when: 'the authorities are mapped'
        def mapped = mapper.mapAuthorities([authority])

        then: 'no ROLE_ authorities are produced'
        roleAuthorities(mapped).isEmpty()
    }

    void "produces no ROLE_ authorities when the client id is missing or blank"() {
        given: 'a mapper whose client id is not configured'
        def mapper = mapperFor(clientId)
        def authority = tokenWithClaims([
                resource_access: [
                        'example-client': [roles: ['USER', 'ADMIN']]
                ]
        ])

        when: 'the authorities are mapped'
        def mapped = mapper.mapAuthorities([authority])

        then: 'no ROLE_ authorities are produced and no exception is thrown'
        roleAuthorities(mapped).isEmpty()

        where:
        clientId << [null, '']
    }

    void "produces no ROLE_ authorities when the resource_access claim is missing"() {
        given: 'an ID token without a resource_access claim'
        def mapper = mapperFor('example-client')
        def authority = tokenWithClaims([sub: 'user-1'])

        when: 'the authorities are mapped'
        def mapped = mapper.mapAuthorities([authority])

        then: 'no ROLE_ authorities are produced and no exception is thrown'
        roleAuthorities(mapped).isEmpty()
    }
}

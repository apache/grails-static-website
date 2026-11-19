package com.grails.example

import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

/**
 * Proves the account pages sit behind the ROLE_USER gate.
 *
 * The controller is autowired so the call goes through the Spring proxy,
 * which is where the @PreAuthorize interceptor fires. Each feature first
 * places a token carrying the desired authorities in the security context,
 * standing in for what Keycloak yields after login.
 */
@Integration
@Rollback
@Import(TestOAuth2ClientConfiguration)
class AccountControllerIntegrationSpec extends Specification implements SecuredRequestSupport {

    @Autowired
    AccountController accountController

    def cleanup() {
        clearAuthContext()
    }

    void "index resolves the account view for an authenticated user with ROLE_USER"() {
        given: 'a request to /account and a mocked token carrying ROLE_USER'
        mockKeycloakUser()
        bindRequest('/account')

        when: 'the secured index action runs'
        accountController.index()

        then: 'the account/index.gsp view resolves with status 200'
        accountController.response.status == 200
        accountController.modelAndView.viewName == '/account/index'
    }

    void "index rejects an authenticated user with only ROLE_ADMIN"() {
        given: 'a request to /account and a mocked token carrying only ROLE_ADMIN'
        mockToken([new SimpleGrantedAuthority('ROLE_ADMIN')])
        bindRequest('/account')

        when: 'the secured index action runs'
        accountController.index()

        then: 'method security denies access'
        thrown(AccessDeniedException)
    }

    void "index rejects an unauthenticated request"() {
        given: 'no authentication token in the security context'
        clearAuthContext()
        bindRequest('/account')

        when: 'the secured index action runs'
        accountController.index()

        then: 'method security denies access'
        thrown(AuthenticationCredentialsNotFoundException)
    }
}

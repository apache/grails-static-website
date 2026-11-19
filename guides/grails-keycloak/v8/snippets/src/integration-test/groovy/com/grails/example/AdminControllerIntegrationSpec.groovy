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
 * Proves the admin pages sit behind the ROLE_ADMIN gate.
 *
 * Same approach as AccountControllerIntegrationSpec: autowire the controller
 * so the proxy enforces @PreAuthorize, then mock the security context.
 */
@Integration
@Rollback
@Import(TestOAuth2ClientConfiguration)
class AdminControllerIntegrationSpec extends Specification implements SecuredRequestSupport {

    @Autowired
    AdminController adminController

    void cleanup() {
        clearAuthContext()
    }

    void "index resolves the admin view for an authenticated user with ROLE_ADMIN"() {
        given: 'a request to /admin and a mocked token carrying ROLE_ADMIN'
        mockToken([new SimpleGrantedAuthority('ROLE_ADMIN')])
        bindRequest('/admin')

        when: 'the secured index action runs'
        adminController.index()

        then: 'the admin/index.gsp view resolves with status 200'
        adminController.response.status == 200
        adminController.modelAndView.viewName == '/admin/index'
    }

    void "index rejects an authenticated user with only ROLE_USER"() {
        given: 'a request to /admin and a mocked token carrying only ROLE_USER'
        mockKeycloakUser()
        bindRequest('/admin')

        when: 'the secured index action runs'
        adminController.index()

        then: 'method security denies access'
        thrown(AccessDeniedException)
    }

    void "index rejects an unauthenticated request"() {
        given: 'no authentication token in the security context'
        clearAuthContext()
        bindRequest('/admin')

        when: 'the secured index action runs'
        adminController.index()

        then: 'method security denies access'
        thrown(AuthenticationCredentialsNotFoundException)
    }
}

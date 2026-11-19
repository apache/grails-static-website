package com.grails.example

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockServletContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

/**
 * Helpers for driving a @PreAuthorize-guarded controller action offline.
 */
trait SecuredRequestSupport {

    /** Puts an authenticated principal carrying the given authorities into the security context. */
    void mockToken(List authorities) {
        SecurityContextHolder.context.authentication =
                new UsernamePasswordAuthenticationToken('mock-user', 'not-a-real-password', authorities)
    }

    /** The ROLE_USER that AccountController's @PreAuthorize requires. */
    void mockKeycloakUser() {
        mockToken([new SimpleGrantedAuthority('ROLE_USER')])
    }

    /** Resets both thread locals. Call from your spec's cleanup(). */
    void clearAuthContext() {
        RequestContextHolder.resetRequestAttributes()
        SecurityContextHolder.clearContext()
    }

    /** Binds a GrailsWebRequest to the thread so the action can resolve a view. */
    void bindRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest('GET', uri)
        MockHttpServletResponse response = new MockHttpServletResponse()
        GrailsWebRequest webRequest = new GrailsWebRequest(request, response, new MockServletContext())
        RequestContextHolder.setRequestAttributes(webRequest)
    }

}

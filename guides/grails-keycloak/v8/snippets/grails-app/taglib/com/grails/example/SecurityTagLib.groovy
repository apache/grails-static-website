package com.grails.example

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class SecurityTagLib {

    static namespace = "app"

    def loggedIn = {attrs, body ->
        def authentication = SecurityContextHolder.context.authentication

        if(authentication && authentication.authenticated &&
           !(authentication instanceof AnonymousAuthenticationToken)) {
            out << body()
        }
    }

    def hasRole = {attrs, body ->
        def authentication = SecurityContextHolder.context.authentication

        if(!authentication || !authentication.authenticated || authentication instanceof AnonymousAuthenticationToken) {
            return
        }

        def role = attrs.role?.toString()?.toUpperCase()

        if(!role) {
            return
        }

        def requiredAuthority = role.startsWith('ROLE_') ? role:"ROLE_${role}"

        if(authentication.authorities.any {
            it.authority == requiredAuthority
        }) {
            out << body()
        }
    }

    def ifNotLoggedIn = {attrs, body ->
        def authentication = SecurityContextHolder.context.authentication

        if(!authentication || !authentication.authenticated || authentication instanceof AnonymousAuthenticationToken) {
            out << body()
        }
    }

    def currentUser = {attrs, body ->
        def authentication = SecurityContextHolder.context.authentication

        if(authentication && authentication.authenticated &&
           !(authentication instanceof AnonymousAuthenticationToken)) {

            def principal = authentication.principal

            if(principal instanceof OidcUser) {
                def user = [username : principal.preferredUsername,
                            firstName: principal.givenName,
                            lastName : principal.familyName,
                            email    : principal.email]

                out << body(user)
            }
        }
    }
}

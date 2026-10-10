package com.grails.example

import org.springframework.security.authorization.AuthorizationDeniedException

class UrlMappings {
    static mappings = {
        "/$namespace/$controller/$action?/$id?(.$format)?" {}
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: "home", action: "index")
        "500"(view: '/error')
        "404"(view: '/notFound')
        "403"(controller: "errors",
              action: "authorizationDenied",
              exception: AuthorizationDeniedException)

        "500"(view: '/notFound', exception: AuthorizationDeniedException)
    }
}

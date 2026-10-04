package example

class UrlMappings {

    static mappings = {
        "/$namespace/$controller/$action?/$id?(.$format)?" {}
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        '/'(controller: 'home')
        '500'(view: '/error')
        '404'(view: '/notFound')
    }
}

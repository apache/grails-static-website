package demo

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        //tag::urlMappings[]
        "/signup"(controller: 'register', action: 'index')
        "/register"(controller: 'register', action: 'save')
        //end::urlMappings[]

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}

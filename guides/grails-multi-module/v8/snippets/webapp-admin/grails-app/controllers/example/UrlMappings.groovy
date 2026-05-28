package example

class UrlMappings {
    static mappings = {
        // REST resource mapping so the admin BookController (a RestfulController)
        // routes GET/POST/PUT/DELETE on /books to its CRUD actions.
        "/books"(resources: 'book')

        "/$namespace/$controller/$action?/$id?(.$format)?" {}
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}

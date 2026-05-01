package demo

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

//tag::delete[]
        delete "/ice-cream/$username/$id"(controller: 'iceCream', action: 'delete')
//end::delete[]

//tag::get[]        
        get "/ice-cream/$username"(controller: 'iceCream', action: 'index')
//end::get[]        

//tag::post[]        
        post "/ice-cream/$username"(controller: 'iceCream', action: 'save')
//end::post[]        

//tag::signup[]        
        post '/signup'(controller: 'signup')
//end::signup[]        

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}

package com.example

class UrlMappings {

    static mappings = {

        // tag::urlMappingsWithResources[]
        "/api/products"(resources: "product")
        // end::urlMappingsWithResources[]
        get "/api/orders/v1/$id"(controller: 'order', action: 'showv1')
        get "/api/orders/v2/$id"(controller: 'order', action: 'showv2')
        get "/api/orders/v3/$id"(controller: 'order', action: 'showv3')
        get "/api/orders/v4/$id"(controller: 'order', action: 'showv4')
        delete "/api/orders/$id(.$format)?"(controller: 'order', action:"delete")
        get "/api/orders(.$format)?"(controller: 'order', action:"index")
        get "/api/orders/$id(.$format)?"(controller: 'order', action:"show")
        post "/api/orders(.$format)?"(controller: 'order', action:"save")
        put "/api/orders/$id(.$format)?"(controller: 'order', action:"update")
        patch "/api/orders/$id(.$format)?"(controller: 'order', action:"patch")

        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")


        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}

package neo4j.movies

class UrlMappings {

    static mappings = {
        //tag::endpoints[]
        "/movie/$title"(controller: 'movie', action: 'show') // <1>
        '/search'(controller: 'movie', action: 'search') // <2>
        '/graph'(controller: 'movie', action: 'graph') // <3>
        //end::endpoints[]

        //tag::index[]
        '/'(uri: '/index.html')
        //end::index[]
        '500'(view: '/error')
        '404'(view: '/notFound')
    }
}

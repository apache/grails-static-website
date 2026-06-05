package neo4j.movies

import groovy.transform.CompileStatic

//tag::controller[]
@CompileStatic
class MovieController {
    static responseFormats = ['json', 'xml']
//end::controller[]

    // tag::service[]
    MovieService movieService
    //end::service[]

    // tag::show[]
    def show(String title) {
        respond movieService.find(title)
    }
    //end::show[]

    // tag::search[]
    def search(String q) {
        respond movieService.search(q)
    }
    //end::search[]

    // tag::graph[]
    def graph() {
        respond movieService.graph(params.int('limit', 100))
    }
    //end::graph[]
}

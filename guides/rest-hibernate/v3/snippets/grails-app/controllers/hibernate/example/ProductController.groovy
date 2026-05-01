package hibernate.example

import grails.rest.RestfulController

//tag::controller[]
import groovy.transform.CompileStatic

@CompileStatic
class ProductController extends RestfulController {
    static responseFormats = ['json', 'xml']
    ProductController() {
        super(Product)
    }
    //end::controller[]

    // tag::searchAction[]
    def search(String q, Integer max ) { // <1>
        if (q) {
            //tag::whereQuery[]
            def query = Product.where { // <2>
                name ==~ "%${q}%"
            }
            //end::whereQuery[]
            //tag::respond[]
            respond query.list(max: Math.min( max ?: 10, 100)) // <3>
            //end::respond[]
        }
        else {
            respond([]) // <4>
        }
    }
    //end::searchAction[]
}

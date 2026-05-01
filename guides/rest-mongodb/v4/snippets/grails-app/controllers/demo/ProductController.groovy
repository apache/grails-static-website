package demo

import grails.rest.RestfulController
import groovy.transform.CompileStatic

//tag::controller[]
@CompileStatic
class ProductController extends RestfulController {
    static responseFormats = ['json', 'xml']
    ProductController() {
        super(Product)
    }
    //end::controller[]
    //tag::searchAction[]
    def search(String q, Integer max ) { // <1>
        if (q) {
            //tag::whereQuery[]
            def query = Product.where { // <2>
                name ==~ ~/$q/
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

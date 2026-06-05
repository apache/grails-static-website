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
    ProductService productService
    //end::controller[]

    // tag::searchAction[]
    def search(String q, Integer max ) { // <1>
        if (q) {
            //tag::respond[]
            respond productService.findByNameLike("%${q}%".toString(), [max: Math.min( max ?: 10, 100)]) // <3>
            //end::respond[]
        }
        else {
            respond([]) // <4>
        }
    }
    //end::searchAction[]
}

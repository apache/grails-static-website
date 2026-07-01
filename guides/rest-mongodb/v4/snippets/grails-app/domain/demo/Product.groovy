package demo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Product {

    String name
    Double price

    //tag::constraints[]
    static constraints = {
        name blank: false
        price range: 0.0..1000.00
    }
    //end::constraints[]
}

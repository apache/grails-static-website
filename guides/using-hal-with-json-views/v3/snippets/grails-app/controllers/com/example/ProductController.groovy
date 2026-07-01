package com.example

import grails.rest.RestfulController

class ProductController extends RestfulController<Product> {
    static responseFormats = ['json']

    ProductController() {
        super(Product)
    }

    @Override
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)

        return [
                productList : listAllResources(params), //<1>
                productCount: countResources(),     //<2>
                max         : params.max,        //<3>
                offset      : params.int("offset") ?: 0, //<4>
                sort        : params.sort,  //<5>
                order       : params.order  //<6>
        ]
    }

    @Override
    boolean getReadOnly() {
        return true
    }
}
package com.example

import grails.rest.*

@Resource(readOnly = true, uri='/api/customers') // <1>
class Customer {

    String firstName
    String lastName

    Address address

    static hasMany = [ orders: Order ]

    static constraints = {
        orders nullable: true
    }
}

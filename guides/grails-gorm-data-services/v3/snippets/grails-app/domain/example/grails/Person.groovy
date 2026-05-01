package example.grails

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Person {

    String name
    Integer age
    Set<Address> addresses // <1>

    static hasMany = [addresses: Address]
}


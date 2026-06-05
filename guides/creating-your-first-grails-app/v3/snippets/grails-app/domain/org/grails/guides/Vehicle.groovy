package org.grails.guides

@SuppressWarnings('GrailsDomainReservedSqlKeywordName')
class Vehicle {

    Integer year

    String name
    Model model
    Make make

    static constraints = {
        year min: 1900
        name maxSize: 255
    }
}

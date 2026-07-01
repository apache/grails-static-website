package demo

import grails.validation.Validateable

class VatCommand implements Validateable {

    String code
    String vatNumber

    static constraints = {
        code nullable: false
        vatNumber nullable: false
    }
}
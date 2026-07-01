package demo

import grails.validation.Validateable

class SignupCommand implements Validateable {
    String username
    String password

    static constraints = {
        username nullable: false, blank: false
        password nullable: false, blank: false
    }
}
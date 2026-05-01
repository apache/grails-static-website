package demo

import grails.plugin.springsecurity.annotation.Secured

class BankController {

    @Secured(['ROLE_CLIENT'])
    def index() {
        render 'Welcome to your bank'
    }
}
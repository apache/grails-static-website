package demo

import groovy.transform.CompileStatic

@CompileStatic
class RegisterController {

    static allowedMethods = [index: 'GET', save: 'POST']

    RegisterService registerService

    def index() {
        [signUpInstance:  new RegisterCommand()]
    }

    def save(RegisterCommand cmd) {

        if ( cmd.hasErrors() ) {
            render view: 'index', model: [signUpInstance: cmd]
            return
        }

        SaveUserResult result = registerService.register(cmd, request.locale)
        flash.message = result.message
        if ( result.error ) {
            flash.error = result.error
            render view: 'index', model: [signUpInstance: cmd]
            return
        }
        redirect action: 'index'
    }
}
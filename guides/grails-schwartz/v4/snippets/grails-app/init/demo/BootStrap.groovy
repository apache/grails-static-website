package demo

import groovy.transform.CompileStatic

@CompileStatic
class BootStrap {

	RegisterService registerService

    def init = { servletContext ->
    	registerService.register('harry@howards.com')
    	sleep(20_000)
    	registerService.register('ron@howards.com')
    }
    def destroy = {
    }
}

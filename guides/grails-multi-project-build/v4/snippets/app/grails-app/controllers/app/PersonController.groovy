package app

import demo.RandomPersonService
import groovy.transform.CompileStatic

@CompileStatic
class PersonController {
    RandomPersonService randomPersonService
    def index() {
        render randomPersonService.randomOciPersonName()
    }
}


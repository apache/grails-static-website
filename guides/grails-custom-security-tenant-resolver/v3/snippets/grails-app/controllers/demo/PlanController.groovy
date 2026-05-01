package demo

import groovy.transform.CompileStatic

@CompileStatic
class PlanController {
    PlanDataService planDataService

    def index() {
        [planList: planDataService.findAll()]
    }
}

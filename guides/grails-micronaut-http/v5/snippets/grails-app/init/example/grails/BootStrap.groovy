package example.grails

import groovy.transform.CompileStatic

@CompileStatic
class BootStrap {

    RecordLabelService recordLabelService

    def init = { servletContext ->
        recordLabelService.save("Warner")
        recordLabelService.save("Sony")
    }
    def destroy = {
    }
}

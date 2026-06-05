package example.grails

import groovy.transform.CompileStatic

@CompileStatic
class CustomError {
    Integer status
    String error
    String message
    String path
}

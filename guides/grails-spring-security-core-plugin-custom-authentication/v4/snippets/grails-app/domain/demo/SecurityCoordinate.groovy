package demo

import groovy.transform.CompileStatic

@CompileStatic
class SecurityCoordinate {
    String position
    String value
    static belongsTo = [user: User]
}
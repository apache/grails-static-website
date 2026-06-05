package grails.mock.basics

import groovy.transform.CompileStatic

@CompileStatic
class Classroom {

    String teacher
    static hasMany = [students: Student]
}

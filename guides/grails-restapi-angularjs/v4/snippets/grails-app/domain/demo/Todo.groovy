package demo

import grails.rest.Resource

@Resource(uri='/todos')
class Todo {
    String name
    boolean completed

    static constraints = {
    }
}

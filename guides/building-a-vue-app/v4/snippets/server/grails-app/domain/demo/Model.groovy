package demo

import grails.rest.Resource

@Resource(uri = '/model')
class Model {
    String name
}

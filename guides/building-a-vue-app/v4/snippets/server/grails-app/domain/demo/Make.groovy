package demo

import grails.rest.Resource

@Resource(uri = '/make')
class Make {
    String name
}

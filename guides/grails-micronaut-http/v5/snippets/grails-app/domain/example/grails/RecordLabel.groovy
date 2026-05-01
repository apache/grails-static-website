package example.grails

import grails.rest.Resource

@Resource(uri='/recordlabels')
class RecordLabel {
    String name
}

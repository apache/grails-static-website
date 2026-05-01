package com.objectcomputing

import grails.rest.Resource

@Resource(uri='/talk')
class Talk {

    String title
    int duration

    static graphql = true //<1>

    static belongsTo = [speaker: Speaker]
}
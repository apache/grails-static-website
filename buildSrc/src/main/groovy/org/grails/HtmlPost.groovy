package org.grails

import groovy.transform.CompileStatic

@CompileStatic
class HtmlPost {

    String path
    PostMetadata metadata
    String html
    Set<String> tags
}

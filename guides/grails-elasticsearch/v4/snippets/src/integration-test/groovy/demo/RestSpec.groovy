package demo

import grails.plugins.rest.client.RequestCustomizer
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import spock.lang.Specification

class RestSpec extends Specification {

    RestBuilder rest = new RestBuilder()

    RestResponse get(String path, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        rest.get("http://localhost:${serverPort}${path}", customizer)
    }
}

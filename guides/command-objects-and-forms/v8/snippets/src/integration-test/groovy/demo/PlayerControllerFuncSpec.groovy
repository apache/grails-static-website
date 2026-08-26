package demo

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Integration
class PlayerControllerFuncSpec extends Specification {

    void 'test save validation'() {
        given:
        HttpClient client = HttpClient.newHttpClient()
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}/player/save.json"))
                .header('Accept', 'application/json')
                .header('Content-Type', 'application/x-www-form-urlencoded')
                .POST(HttpRequest.BodyPublishers.ofString('name=Bob+Smith&wins=42&losses=abc'))
                .build()

        when:
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString())
        Map body = new JsonSlurper().parseText(resp.body()) as Map
        List errors = body.errors as List

        then:
        resp.statusCode() == 422 // <4>
        errors.find { it.field == 'losses' }.message == 'Property losses is type-mismatched'
    }
}

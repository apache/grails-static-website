// tag::saveSpec[]
package demo

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Integration
class PlayerControllerFuncSpec extends Specification {

    HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()

    void 'test save validation'() {
        given:
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}/player/save.json")) // <1>
                .header('Accept', 'application/json') // <2>
                .header('Content-Type', 'application/x-www-form-urlencoded') // <3>
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
    // end::saveSpec[]

    // tag::updateIgnores[]
    void 'test update ignores wins and losses'() {
        when: 'a player is created with known wins and losses'
        HttpResponse<String> createdResp = postForm('/player/save.json', 'name=Mass+Assignment&game=Chess&region=NORTH&wins=10&losses=5')
        def id = idFrom(createdResp)

        then:
        createdResp.statusCode() in [201, 302]
        id != null

        when: 'the saved player is fetched'
        Map created = getJson("/player/show/${id}.json")

        then:
        created.wins == 10
        created.losses == 5

        when: 'a crafted request tries to change wins and losses'
        HttpResponse<String> updateResp = postForm('/player/update.json', "id=${id}&name=Mass+Assignment&game=Go&region=EAST&wins=2&losses=194")
        Map after = getJson("/player/show/${id}.json")

        then: 'editable fields change, wins and losses do not'
        updateResp.statusCode() in [200, 302]
        after.name == 'Mass Assignment'
        after.game == 'Go'
        after.region == 'EAST'
        after.wins == 10
        after.losses == 5
    }

    private HttpResponse<String> postForm(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}${path}"))
                .header('Accept', 'application/json')
                .header('Content-Type', 'application/x-www-form-urlencoded')
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private Map getJson(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:${serverPort}${path}"))
                .header('Accept', 'application/json')
                .GET()
                .build()
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString())
        assert resp.statusCode() == 200
        new JsonSlurper().parseText(resp.body()) as Map
    }

    private static Serializable idFrom(HttpResponse<String> resp) {
        if (resp.statusCode() == 201 && resp.body()) {
            return new JsonSlurper().parseText(resp.body()).id
        }
        resp.headers().firstValue('Location').orElse(null)?.split('/')?.last()
    }
    // end::updateIgnores[]

// tag::saveSpec[]
}
// end::saveSpec[]

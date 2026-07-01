package example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Functional tests for the Spring Boot Actuator health surface this guide
 * configures - the same endpoints the Docker HEALTHCHECK and a Kubernetes
 * readiness/liveness probe hit. @Integration boots the app against a
 * Testcontainers PostgreSQL database, so the readiness probe (which checks
 * the datasource) reports UP.
 *
 * No Geb here: the deployment contract is HTTP/JSON, not a browser page.
 */
@Integration
class ActuatorHealthFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    HttpClient client = HttpClient.newHttpClient()

    private HttpResponse<String> get(String path) {
        client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:${serverPort}${path}")).GET().build(),
                HttpResponse.BodyHandlers.ofString())
    }

    // Spring Boot's health JSON nests the status as {code, description}, so
    // the live status string is json.status.code.
    private static String statusCode(String body) {
        (new JsonSlurper().parseText(body) as Map).status.code
    }

    void "GET /actuator/health reports UP"() {
        when:
        HttpResponse<String> resp = get('/actuator/health')

        then:
        resp.statusCode() == 200
        statusCode(resp.body()) == 'UP'
    }

    void "the liveness probe is exposed and UP"() {
        when:
        HttpResponse<String> resp = get('/actuator/health/liveness')

        then:
        resp.statusCode() == 200
        statusCode(resp.body()) == 'UP'
    }

    void "the readiness probe is exposed and UP against a real database"() {
        when:
        HttpResponse<String> resp = get('/actuator/health/readiness')

        then:
        resp.statusCode() == 200
        statusCode(resp.body()) == 'UP'
    }

    void "operator-grade endpoints stay closed"() {
        expect: 'only health and info are on the public surface'
        get('/actuator/env').statusCode() == 404
        get('/actuator/metrics').statusCode() == 404
    }
}

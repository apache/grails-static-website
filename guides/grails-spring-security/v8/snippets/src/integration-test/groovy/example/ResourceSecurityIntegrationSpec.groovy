package example

import grails.testing.mixin.integration.Integration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@Integration
class ResourceSecurityIntegrationSpec extends Specification {

    RestTemplate restTemplate = new RestTemplate()

    private String baseUrl() {
        "http://localhost:${serverPort}"
    }

    private HttpHeaders basicAuthHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders()
        headers.setBasicAuth(username, password)
        headers
    }

    private HttpEntity<String> jsonEntity(Map body, HttpHeaders headers = new HttpHeaders()) {
        headers.setContentType(MediaType.APPLICATION_JSON)
        new HttpEntity(groovy.json.JsonOutput.toJson(body), headers)
    }

    private void seedUsersAndResource() {
        UserRole.withTransaction {
            UserRole.list()*.delete(flush: true)
            User.list()*.delete(flush: true)
            Role.list()*.delete(flush: true)
            Resource.list()*.delete(flush: true)

            def userRole = new Role(authority: 'ROLE_USER').save(failOnError: true, flush: true)
            def adminRole = new Role(authority: 'ROLE_ADMIN').save(failOnError: true, flush: true)

            def user = new User(username: 'user', password: 'password', enabled: true).save(failOnError: true, flush: true)
            UserRole.create(user, userRole, true)

            def admin = new User(username: 'admin', password: 'password', enabled: true).save(failOnError: true, flush: true)
            UserRole.create(admin, adminRole, true)

            new Resource(name: 'Secured Notes', description: 'Used in security integration tests').save(failOnError: true, flush: true)
        }
    }

    void 'anonymous users can read the public resource index'() {
        given:
        seedUsersAndResource()

        when:
        ResponseEntity<String> response = restTemplate.getForEntity("${baseUrl()}/api/resources", String)

        then:
        response.statusCode == HttpStatus.OK
    }

    void 'anonymous POST receives 401 unauthorized'() {
        given:
        seedUsersAndResource()

        when:
        restTemplate.postForEntity("${baseUrl()}/api/resources", jsonEntity([name: 'New Resource']), String)

        then:
        HttpClientErrorException ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.UNAUTHORIZED
    }

    void 'authenticated user with ROLE_USER can create resources'() {
        given:
        seedUsersAndResource()

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                "${baseUrl()}/api/resources",
                HttpMethod.POST,
                jsonEntity([name: 'User Created'], basicAuthHeaders('user', 'password')),
                String
        )

        then:
        response.statusCode == HttpStatus.CREATED
        new groovy.json.JsonSlurper().parseText(response.body).name == 'User Created'
    }

    void 'authenticated user without ROLE_ADMIN receives 403 on update'() {
        given:
        seedUsersAndResource()
        Long resourceId = Resource.withTransaction { Resource.first().id }

        when:
        restTemplate.exchange(
                "${baseUrl()}/api/resources/${resourceId}",
                HttpMethod.PUT,
                jsonEntity([name: 'Blocked Update'], basicAuthHeaders('user', 'password')),
                String
        )

        then:
        HttpClientErrorException ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.FORBIDDEN
    }

    void 'admin can update and delete resources'() {
        given:
        seedUsersAndResource()
        Long resourceId = Resource.withTransaction { Resource.first().id }

        when:
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "${baseUrl()}/api/resources/${resourceId}",
                HttpMethod.PUT,
                jsonEntity([name: 'Admin Updated'], basicAuthHeaders('admin', 'password')),
                String
        )

        then:
        updateResponse.statusCode == HttpStatus.OK
        new groovy.json.JsonSlurper().parseText(updateResponse.body).name == 'Admin Updated'

        when:
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "${baseUrl()}/api/resources/${resourceId}",
                HttpMethod.DELETE,
                new HttpEntity<>(basicAuthHeaders('admin', 'password')),
                String
        )

        then:
        deleteResponse.statusCode == HttpStatus.NO_CONTENT
    }

    void 'invalid create payload returns 422'() {
        given:
        seedUsersAndResource()

        when:
        restTemplate.exchange(
                "${baseUrl()}/api/resources",
                HttpMethod.POST,
                jsonEntity([description: 'missing name'], basicAuthHeaders('user', 'password')),
                String
        )

        then:
        HttpClientErrorException ex = thrown(HttpClientErrorException)
        ex.statusCode.value() == 422
    }
}

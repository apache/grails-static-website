package demo

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.testing.mixin.integration.Integration
import spock.lang.Shared
import spock.lang.Specification

import grails.transaction.Rollback

@Integration
@Rollback
class StudentControllerIntSpec extends Specification {

    @Shared RestBuilder rest = new RestBuilder()

    def setup() {
        Student.saveAll(
                new Student(name: 'Nirav', grade: 100),
                new Student(name: 'Jeff', grade: 95),
                new Student(name: 'Sergio', grade: 90),
        )
    }

    def 'test json in URI to return students'() {
        when:
        RestResponse resp = rest.get("http://localhost:${serverPort}/student.json") // <1> <2>

        then:
        resp.status == 200 // <3>
        resp.json.size() == 3
        resp.json.find { it.grade == 100 && it.name == 'Nirav' }
        resp.json.find { it.grade == 95 &&  it.name == 'Jeff' }
        resp.json.find { it.grade == 90 &&  it.name == 'Sergio' }
    }
}

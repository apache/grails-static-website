package demo

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Shared
import spock.lang.Specification

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitPublicProperty'])
@Integration
class StudentControllerIntSpec extends Specification {

    @Shared HttpClient client

    StudentService studentService

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    def 'test json in URI to return students'() {
        given:
        List<Serializable> ids = []
        Student.withNewTransaction {
            ids << studentService.save('Nirav', 100 as BigDecimal).id
            ids << studentService.save('Jeff', 95 as BigDecimal).id
            ids << studentService.save('Sergio', 90 as BigDecimal).id
        }

        expect:
        studentService.count() == 3

        when:
        HttpRequest request = HttpRequest.GET('/student.json') // <1>
        HttpResponse<List<Map>> resp = client.toBlocking().exchange(request, Argument.of(List, Map)) // <2>

        then:
        resp.status == HttpStatus.OK // <3>
        resp.body()
        resp.body().size() == 3
        resp.body().find { it.grade == 100 && it.name == 'Nirav' }
        resp.body().find { it.grade == 95 &&  it.name == 'Jeff' }
        resp.body().find { it.grade == 90 &&  it.name == 'Sergio' }

        cleanup:
        Student.withNewTransaction {
            ids.each { id ->
                studentService.delete(id)
            }
        }
    }
}

package example.grails

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class RecordLabelControllerSpec extends Specification {

    @Shared
    HttpClient client

    @OnceBefore
    void init() { // <1>
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL())
    }

    void "test rest get record labels"() {
        when:"record labels exist"
        HttpResponse<List<Map>> resp = client.toBlocking().exchange(HttpRequest.GET("/recordlabels"), Argument.of(List, Map)) // <2>

        then: "client can retrieve them"
        resp.status == HttpStatus.OK  // <3>
        resp.body().size() == 2
        resp.body()[0].name == "Warner"
        resp.body()[1].name == "Sony"
    }

    void "test rest post record labels"() {
        when:"a post is issued"
        HttpResponse<Map> resp = client.toBlocking().exchange(HttpRequest.POST("/recordlabels", [name: "Universal"]), Map) // <4>

        then: "element is created"
        resp.status == HttpStatus.CREATED // <5>
        resp.body().size() == 2
        resp.body().id
        resp.body().name == "Universal"

    }
}

package com.example

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Ignore
import spock.lang.Specification

@Integration
class OrderShowSpec extends Specification {
    BlockingHttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL()).toBlocking()
    }

    @Ignore
    def "test order JSON output is as expected"() {
        when:
        def expectedJsonString =
// tag::orderJSON[]
'''
{
   "id" : 1,
   "shippingAddress" : {
      "id" : 1
   },
   "shippingCost" : 13.54,
   "orderId" : "0A12321",
   "orderPlaced" : "2017-02-08T09:10:36Z",
   "products" : [
      {
         "id" : 11
      },
      {
         "id" : 6
      },
      {
         "id" : 1
      }
   ],
   "customer" : {
      "id" : 1
   }
}
'''
// end::orderJSON[]
        HttpResponse<String> resp = client.exchange(HttpRequest.GET('/api/orders/1'), String)
        JSONAssert.assertEquals(expectedJsonString, resp.body.get().toString(), false)

        then:
        resp.status == HttpStatus.OK
        notThrown AssertionError


    }
}

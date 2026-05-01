package com.example

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.uri.UriBuilder
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

@Integration
class ProductsShowFuncSpec extends Specification {
    BlockingHttpClient client

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        this.client  = HttpClient.create(baseUrl.toURL()).toBlocking()
    }

    def "test _embedded appear in JSON"() {
        when:
        HttpRequest request = HttpRequest.GET(UriBuilder.of('/api/products/1')
                .queryParam('lang', 'en')
                .build())
        HttpResponse<String> resp = client.exchange(request, String)
        def expectedJsonString = """
{
  "_links": {
    "self": {
      "href": "http://localhost:${serverPort}/api/products/1",
      "hreflang": "en",
      "type": "application/hal+json"
    }
  },
  "_embedded": {
    "category": {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/categories/1",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Clothing",
      "version": 0
    }
  },
  "id": "CLOTH001",
  "name": "Cargo Pants",
  "price": 15.00
}
"""
        JSONAssert.assertEquals(expectedJsonString, resp.body().toString(), false)

        then:
        notThrown AssertionError
    }
}

package com.example

import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

@Integration
@Rollback
class ProductsShowFuncSpec extends Specification {

    def "test _embedded appear in JSON"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def resp = rest.get("http://localhost:${serverPort}/api/products/1?lang=en") {
            header("Accept", "application/json")
        }

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
        JSONAssert.assertEquals(expectedJsonString, resp.json.toString(), false)

        then:
        notThrown AssertionError
    }
}

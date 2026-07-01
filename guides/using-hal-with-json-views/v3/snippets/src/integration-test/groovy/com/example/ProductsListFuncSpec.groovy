package com.example

import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

@Integration
@Rollback
class ProductsListFuncSpec extends Specification {

    def "test pagination links appear in JSON"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def resp = rest.get("http://localhost:${serverPort}/api/products?lang=en") {
            header("Accept", "application/json")
        }

        def expectedJsonString = // tag::paginatedJSON[]
"""
{
  "_links": {
    "self": {
      "href": "http://localhost:${serverPort}/api/products?offset=0&max=10",
      "hreflang": "en",
      "type": "application/hal+json"
    },
    "first": {
      "href": "http://localhost:${serverPort}/api/products?offset=0&max=10",
      "hreflang": "en"
    },
    "next": {
      "href": "http://localhost:${serverPort}/api/products?offset=10&max=10",
      "hreflang": "en"
    },
    "last": {
      "href": "http://localhost:${serverPort}/api/products?offset=10&max=10",
      "hreflang": "en"
    }
  },
  "products": [
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/1",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Cargo Pants",
      "id": "CLOTH001",
      "price": 15.00,
      "category": "Clothing"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/2",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Sweater",
      "id": "CLOTH002",
      "price": 12.00,
      "category": "Clothing"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/3",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Jeans",
      "id": "CLOTH003",
      "price": 15.00,
      "category": "Clothing"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/4",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Blouse",
      "id": "CLOTH004",
      "price": 18.00,
      "category": "Clothing"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/5",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "T-Shirt",
      "id": "CLOTH005",
      "price": 10.00,
      "category": "Clothing"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/6",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Jacket",
      "id": "CLOTH006",
      "price": 20.00,
      "category": "Clothing"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/7",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Bookcase",
      "id": "FURN001",
      "price": 40.00,
      "category": "Furniture"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/8",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Coffee Table",
      "id": "FURN002",
      "price": 50.00,
      "category": "Furniture"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/9",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Vanity",
      "id": "FURN003",
      "price": 90.00,
      "category": "Furniture"
    },
    {
      "_links": {
        "self": {
          "href": "http://localhost:${serverPort}/api/products/10",
          "hreflang": "en",
          "type": "application/hal+json"
        }
      },
      "name": "Table Saw",
      "id": "TOOL001",
      "price": 120.00,
      "category": "Tools"
    }
  ],
  "count": 13,
  "max": 10,
  "offset": 0,
  "sort": null,
  "order": null
}
"""// end::paginatedJSON[]
        JSONAssert.assertEquals(expectedJsonString, resp.json.toString(), true)

        then:
        notThrown AssertionError
    }
}

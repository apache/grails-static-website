package com.example

import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.json.JsonSlurper
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

@Integration
@Rollback
class ProductsListSpec extends Specification {

    def "test /api/products JSON output is as expected"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def expectedJsonString =
// tag::productsJSON[]
'''
[
  {
    "id": 1,
    "category": {
      "id": 1
    },
    "inventoryId": "CLOTH001",
    "name": "Cargo Pants",
    "price": 15.00
  },
  {
    "id": 2,
    "category": {
      "id": 1
    },
    "inventoryId": "CLOTH002",
    "name": "Sweater",
    "price": 12.00
  },
  {
    "id": 3,
    "category": {
      "id": 1
    },
    "inventoryId": "CLOTH003",
    "name": "Jeans",
    "price": 15.00
  },
  {
    "id": 4,
    "category": {
      "id": 1
    },
    "inventoryId": "CLOTH004",
    "name": "Blouse",
    "price": 18.00
  },
  {
    "id": 5,
    "category": {
      "id": 1
    },
    "inventoryId": "CLOTH005",
    "name": "T-Shirt",
    "price": 10.00
  },
  {
    "id": 6,
    "category": {
      "id": 1
    },
    "inventoryId": "CLOTH006",
    "name": "Jacket",
    "price": 20.00
  },
  {
    "id": 7,
    "category": {
      "id": 2
    },
    "inventoryId": "FURN001",
    "name": "Bookcase",
    "price": 40.00
  },
  {
    "id": 8,
    "category": {
      "id": 2
    },
    "inventoryId": "FURN002",
    "name": "Coffee Table",
    "price": 50.00
  },
  {
    "id": 9,
    "category": {
      "id": 2
    },
    "inventoryId": "FURN003",
    "name": "Vanity",
    "price": 90.00
  },
  {
    "id": 10,
    "category": {
      "id": 3
    },
    "inventoryId": "TOOL001",
    "name": "Table Saw",
    "price": 120.00
  }
]
'''
// end::productsJSON[]
        def resp = rest.get("http://localhost:${serverPort}/api/products") {
            header("Accept", "application/json")
        }
        JSONAssert.assertEquals(expectedJsonString, resp.json.toString(), false)

        then:
        resp.status == 200
        notThrown AssertionError


    }
}

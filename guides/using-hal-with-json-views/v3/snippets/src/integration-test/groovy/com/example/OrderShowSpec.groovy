package com.example

import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.json.JsonSlurper
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

@Integration
@Rollback
class OrderShowSpec extends Specification {

    def "test order JSON output is as expected"() {
        given:
        RestBuilder rest = new RestBuilder()

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
        def resp = rest.get("http://localhost:${serverPort}/api/orders/1") {
            header("Accept", "application/json")
        }
        JSONAssert.assertEquals(expectedJsonString, resp.json.toString(), false)

        then:
        resp.status == 200
        notThrown AssertionError


    }
}

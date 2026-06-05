package com.example

import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

@Integration
@Rollback
class OrderShowV2IntegrationSpec extends Specification {

    def "test _links appear in JSON"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def resp = rest.get("http://localhost:${serverPort}/api/orders/v2/1?lang=en") {
            header("Accept", "application/json")
        }

        def expectedJsonString = """
        {
            _links: {
                self: {
                    href: "http://localhost:${serverPort}/api/orders?id=1",
                    hreflang: "en",
                    type: "application/hal+json"
                },
                customer: {
                    href: "http://localhost:${serverPort}/api/customers?id=1",
                    hreflang: "en",
                    type: "application/hal+json"
                }
            },
            id: "0A12321",
            shippingCost: 13.54,
            date: "2-08-2017",
            shippingAddress: {
                street: "321 Arrow Ln",
                street2: null,
                city: "Chicago",
                state: "IL",
                zip: 646465
            },
            products: [
                    {
                        id: 11
                    },
                    {
                        id: 1
                    },
                    {
                        id: 6
                    }
            ],
            customer: {
                id: 1
            }
        }
        """
        JSONAssert.assertEquals(expectedJsonString, resp.json.toString(), false)

        then:
        notThrown AssertionError
    }
}

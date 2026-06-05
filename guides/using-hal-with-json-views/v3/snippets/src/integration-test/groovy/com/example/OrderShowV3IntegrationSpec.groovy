package com.example

import grails.plugins.rest.client.RestBuilder
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

@Integration
@Rollback
class OrderShowV3IntegrationSpec extends Specification {

    def "test collection _links appear in JSON"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def resp = rest.get("http://localhost:${serverPort}/api/orders/v3/1?lang=en") {
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
                    _links: {
                        self: {
                            href: "http://localhost:${serverPort}/api/products/11",
                            hreflang: "en",
                            type: "application/hal+json"
                        }
                    },
                    id: 11
                },
                {
                    _links: {
                        self: {
                            href: "http://localhost:${serverPort}/api/products/6",
                            hreflang: "en",
                            type: "application/hal+json"
                        }
                    },
                    id: 6
                },
                {
                    _links: {
                        self: {
                            href: "http://localhost:${serverPort}/api/products/1",
                            hreflang: "en",
                            type: "application/hal+json"
                        }
                    },
                    id: 1
                }
            ]
        }
        """
        JSONAssert.assertEquals(expectedJsonString, resp.json.toString(), false)

        then:
        notThrown AssertionError
    }
}

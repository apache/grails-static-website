package com.example

import grails.plugin.json.view.test.JsonViewTest
import org.skyscreamer.jsonassert.JSONAssert
import grails.test.hibernate.HibernateSpec

class OrderShowV1Spec extends HibernateSpec implements JsonViewTest {

    def "test json output uses custom id instead of database id"() {
        setup:
        def (Category clothing, Category furniture, Category tools) = BootStrap.fixtureCategories()
        def products = BootStrap.fixtureProducts(clothing, furniture, tools)
        def customers = BootStrap.fixtureCustomers()
        def orders  = BootStrap.fixtureOrders(products, customers)

        when:
        def order = orders.first()
        def result = render(view: "/order/show_v1", model:[order: order])

        then:
        result.json.id == '0A12321'

        when:
        def expectedJsonString = '''
        {
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
        '''
        JSONAssert.assertEquals(expectedJsonString, result.jsonText, false)

        then:
        notThrown AssertionError
    }
}

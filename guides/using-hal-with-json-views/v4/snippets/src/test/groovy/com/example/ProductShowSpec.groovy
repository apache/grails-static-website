package com.example

import grails.plugin.json.view.test.JsonViewTest
import grails.test.hibernate.HibernateSpec
import org.skyscreamer.jsonassert.JSONAssert

class ProductShowSpec extends HibernateSpec implements JsonViewTest {

    def "test _embedded appear in JSON"() {
        setup:
        mappingContext.addPersistentEntity(Category)

        def (Category clothing, Category furniture, Category tools) = BootStrap.fixtureCategories()
        def products = BootStrap.fixtureProducts(clothing, furniture, tools)
        def customers = BootStrap.fixtureCustomers()
        BootStrap.fixtureOrders(products, customers)

        when:
        def product = products.first()
        def result = render(view: "/product/show", model:[product: product])

        then:
        result.json._embedded
    }
}

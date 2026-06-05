package hibernate.example

import grails.plugin.json.view.mvc.JsonViewResolver
import grails.test.hibernate.HibernateSpec
import grails.testing.web.controllers.ControllerUnitTest

//tag::spec[]
@SuppressWarnings('MethodName')
class ProductControllerSpec extends HibernateSpec implements ControllerUnitTest<ProductController> {
//end::spec[]

    //tag::config[]
    static doWithSpring = {
        jsonSmartViewResolver(JsonViewResolver)
    }
    //end::config[]

    //tag::setup[]
    void setup() {
        Product.saveAll(
            new Product(name: 'Apple', price: 2.0),
            new Product(name: 'Orange', price: 3.0),
            new Product(name: 'Banana', price: 1.0),
            new Product(name: 'Cake', price: 4.0)
        )
    }
    //end::setup[]

    //tag::test[]
    void 'test the search action finds results'() {
        when: 'A query is executed that finds results'
        controller.search('pp', 10)

        then: 'The response is correct'
        response.json.size() == 1
        response.json[0].name == 'Apple'
    }
    //tag::test[]
}

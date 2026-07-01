package demo

import spock.lang.Ignore
//tag::imports[]
import com.github.fakemongo.Fongo
import com.mongodb.MongoClient
import grails.test.mongodb.MongoSpec
import grails.testing.web.controllers.ControllerUnitTest
//end::imports[]

@Ignore
@SuppressWarnings('MethodName')
//tag::clazz[]
class ProductControllerSpec extends MongoSpec // <1>
        implements ControllerUnitTest<ProductController> {  // <2>

    @Override
    MongoClient createMongoClient() {  // <3>
        new Fongo(getClass().name).mongo
    }
    void setup() { // <4>
        Product.saveAll(
            new Product(name: 'Apple', price: 2.0),
            new Product(name: 'Orange', price: 3.0),
            new Product(name: 'Banana', price: 1.0),
            new Product(name: 'Cake', price: 4.0)
        )
    }
    void 'test the search action finds results'() {
        when: 'A query is executed that finds results'
        controller.search('pp', 10)

        then: 'The response is correct'
        response.json.size() == 1
        response.json[0].name == 'Apple'
    }
}
//end::clazz[]
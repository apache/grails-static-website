package demo

import org.springframework.transaction.annotation.Transactional

class DemoService {

    @Transactional
    void myMethod() {
        println 'Some business logic'
    }
}

package example.grails

import groovy.transform.CompileStatic

@CompileStatic
class Person {
    String name
    Integer age

    String toString() {
        "name: $name, age: $age"
    }
}

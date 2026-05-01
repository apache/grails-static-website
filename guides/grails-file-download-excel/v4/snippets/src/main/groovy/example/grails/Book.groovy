package example.grails

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@CompileStatic
@EqualsAndHashCode
@TupleConstructor
class Book {
    String isbn
    String name
}
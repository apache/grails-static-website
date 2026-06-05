package example.grails

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor // <1>
@CompileStatic
class Country {
    String name
}

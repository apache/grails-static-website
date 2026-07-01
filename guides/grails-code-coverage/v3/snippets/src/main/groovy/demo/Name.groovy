package demo

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class Name {
    String firstName
    String lastName
}

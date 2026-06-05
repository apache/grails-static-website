package demo

import groovy.transform.CompileStatic

@CompileStatic
class Student {
    String name
    BigDecimal grade

    String toString() {
        "${name}-Grade:$grade"
    }
}

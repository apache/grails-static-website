package demo

import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@GrailsCompileStatic
class StudentSaveCommand implements Validateable {
    String name
    BigDecimal grade

    static constraints = {
        name nullable: false
        grade nullable: false, min: 0.0
    }
}

package grails.mock.basics

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional


@GrailsCompileStatic
@Transactional(readOnly = true)
class StudentService {

    List<Student> findStudentsWithGradeAbove(BigDecimal grade) {
        Student.findAllByGradeGreaterThanEquals(grade)
    }
}

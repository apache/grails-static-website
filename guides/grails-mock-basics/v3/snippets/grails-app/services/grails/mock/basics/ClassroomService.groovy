package grails.mock.basics

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@CompileStatic
@Transactional(readOnly = true)
class ClassroomService {

    BigDecimal calculateAvgGrade(String teacherName) {
        Student.where {
            classroom.teacher == teacherName
        }.projections {
            avg('grade')
        }.get() as BigDecimal
    }
}

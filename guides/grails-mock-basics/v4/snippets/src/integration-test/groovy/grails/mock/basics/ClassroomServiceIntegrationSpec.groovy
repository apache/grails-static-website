package grails.mock.basics

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@SuppressWarnings('MethodName')
@Rollback
@Integration
class ClassroomServiceIntegrationSpec extends Specification {

    @Autowired ClassroomService service

    void 'test calculate average grade of classroom'() {
        when:
        def classroom = new Classroom(teacher: 'Smith')
        [
                [name: 'Nirav', grade: 91],
                [name: 'Sergio', grade: 95],
                [name: 'Jeff', grade: 93],
        ].each {
            classroom.addToStudents(new Student(name: it.name, grade: it.grade))
        }
        classroom.save()

        then:
        Classroom.count() == 1
        Student.count() == 3

        when:
        BigDecimal avgGrade = service.calculateAvgGrade('Smith')

        then:
        avgGrade == 93.0
    }
}

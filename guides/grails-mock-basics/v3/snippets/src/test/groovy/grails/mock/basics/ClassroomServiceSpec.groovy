package grails.mock.basics

import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest

@SuppressWarnings('MethodName')
class ClassroomServiceSpec extends HibernateSpec implements ServiceUnitTest<ClassroomService> {

    List<Class> getDomainClasses() { [Student, Classroom] }

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

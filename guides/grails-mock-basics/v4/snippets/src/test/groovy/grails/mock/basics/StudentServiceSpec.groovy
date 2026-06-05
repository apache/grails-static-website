package grails.mock.basics

import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest

@SuppressWarnings(['MethodName', 'DuplicateNumberLiteral'])
class StudentServiceSpec extends HibernateSpec implements ServiceUnitTest<StudentService> {

    List<Class> getDomainClasses() { [Student] } // <1>
    def 'test find students with grades above'() {
        when: 'students are already stored in db'
        Student.saveAll(
            new Student(name: 'Nirav', grade: 91),
            new Student(name: 'Sergio', grade: 95),
            new Student(name: 'Jeff', grade: 93),
        )

        then:
        Student.count() == 3

        when: 'service is called to search'
        List<Student> students = service.findStudentsWithGradeAbove(92)

        then: 'students are found with appropriate grades'
        students.size() == 2
        students[0].name == 'Sergio'
        students[0].grade == 95
        students[1].name == 'Jeff'
        students[1].grade == 93
    }
}

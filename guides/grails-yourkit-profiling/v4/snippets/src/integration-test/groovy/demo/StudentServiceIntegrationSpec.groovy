package demo

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Rollback
@Integration
class StudentServiceIntegrationSpec extends Specification {

    StudentService studentService

    def 'test insertStudents'() {
        when:
        studentService.insertStudents(100)

        then:
        Student.count() == old(Student.count()) + 100
    }

    def 'test deleteStudents'() {
        when:
        studentService.insertStudents(100)
        studentService.deleteStudentsWithGradleLessThanA()

        then:
        Student.count() < (old(Student.count()) + 100)
    }

    def 'test htmlUnorderedListOfStudents'() {
        when:
        studentService.insertStudents(100)
        String results = studentService.htmlUnorderedListOfStudents()

        then:
        !results.isEmpty()
        results.count('<ul>') == 1
        results.count('<li>') == 100
        results.count('</li>') == 100
        results.count('</ul>') == 1
    }

    def 'test saveExcelStudents'() {
        when:
        studentService.saveExcelStudents("src/integration-test/resources/studentImport-3rows.xlsx")

        then:
        Student.count() == old(Student.count()) + 3
    }

}
package demo

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class StudentServiceSpec extends Specification implements ServiceUnitTest<StudentService> {

    def 'test produceRandomName'() {
        when:
        String name = service.produceRandomName()

        then:
        !name.isEmpty()
    }

    def 'test importStudents'() {
        when:
        List<Map> studentData = service.importStudents("src/test/resources/studentImport-3rows.xlsx")

        then:
        studentData.size() == 3
    }
}

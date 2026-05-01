// tag::unitTestPackageImport[]
package demo
// end::unitTestPackageImport[]

// tag::unitTestImports[]
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
// end::unitTestImports[]

// tag::unitTestClassDeclaration[]
class StudentControllerSpec extends Specification implements ControllerUnitTest<StudentController> {
// end::unitTestClassDeclaration[]

    private interface StudentService {
        List list()
        Number count()
    }

    // tag::testIndex[]
    def "index action returns the expected model"() {

        given:
        List<Map<String, String>> students = [
                [name: 'Sergio del Amo'],
                [name: 'Graeme Rocher']
        ]
        controller.studentService = Stub(StudentService) {
            list() >> students
            count() >> students.size()
        }

        when:
        def model = controller.index()

        then:
        model.studentList == students
        model.studentCount == students.size() // <1>
    }
    // end::testIndex[]

// tag::specEnding[]
}
// end::specEnding[]

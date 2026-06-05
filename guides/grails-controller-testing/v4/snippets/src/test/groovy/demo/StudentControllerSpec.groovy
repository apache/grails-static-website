// tag::unitTestPackageImport[]
package demo

// end::unitTestPackageImport[]

// tag::unitTestImports[]
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

// end::unitTestImports[]

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitPublicProperty'])
// tag::unitTestClassDeclaration[]
class StudentControllerSpec extends Specification implements ControllerUnitTest<StudentController> {
// end::unitTestClassDeclaration[]

    // tag::testIndex[]
    def 'Test the index action returns the correct model'() {
        given:
        List<Student> sampleStudents = [new Student(name: 'Nirav', grade: 100),
                                        new Student(name: 'Jeff', grade: 95),
                                        new Student(name: 'Sergio', grade: 90),]
        controller.studentService = Stub(StudentService) {
            list(_) >> sampleStudents
            count() >> sampleStudents.size()
        }

        when: 'The index action is executed'
        controller.index()

        then: 'The model is correct'
        model.studentList // <1>
        model.studentList.size() == sampleStudents.size()
        model.studentList.find { it.name == 'Nirav' && it.grade == 100 }
        model.studentList.find { it.name == 'Jeff' && it.grade == 95 }
        model.studentList.find { it.name == 'Sergio' && it.grade == 90 }
        model.studentCount == sampleStudents.size()
    }
    // end::testIndex[]

    // tag::testShow[]
    def 'show action returns 404, if the user does not supply the student ID'() {
        when:
        controller.show()

        then:
        response.status == 404
    }

    def 'show action returns the correct model'() {
        given:
        String name = 'Nirav'
        BigDecimal grade = 100
        Long id = 1L
        controller.studentService = Stub(StudentService) {
            read(_) >> new Student(name: name, grade: grade, id: id)
        }
        when:
        params['id'] = id
        controller.show()

        then:
        model.student
        model.student.name == name
        model.student.grade == grade
    }
    // end::testShow[]

    // tag:testCreate[]
    def 'Test the create action returns the correct model'() {
        given:
        controller.studentService = Stub(StudentService) {
            create(_) >> new Student()
        }
        when: 'The create action is executed'
        controller.create()

        then: 'A student instantiated by the service layer is passed to the model'
        model.student
    }
    // end:testCreate[]

    // tag::testSave1[]
    def 'If you save without supplying name and grade(both required) you remain in the create form'() {

        when:
        request.contentType = FORM_CONTENT_TYPE // <1>
        request.method = 'POST' // <2>
        controller.save()

        then:
        model.student
        view == 'create' // <3>
    }
    // end::testSave1[]

    // tag::testSave2[]
    def 'if the users supplies both name and grade, save is successful '() {
        given:
        String name = 'Nirav'
        BigDecimal grade = 100
        Long id = 1L
        controller.studentService = Stub(StudentService) {
            save(_, _) >> new Student(name: name, grade: grade, id: id)
            read(_) >> new Student(name: name, grade: grade, id: id)
        }
        when:
        request.method = 'POST'
        request.contentType = FORM_CONTENT_TYPE
        params['name'] =  name // <1>
        params['grade'] = grade
        controller.save() // <2>

        then: 'a message indicating that the user has been saved is placed'
        flash.message // <3>

        and: 'the user is redirected to show the student'
        response.redirectedUrl.startsWith('/student/show') // <4>

        and: 'a found response code is used'
        response.status == 302 // <5>
    }
    // end::testSave2[]

    // tag::testSave3[]
    void 'JSON payload is bound to the command object. If the student is saved, a 201 is returned'() {
        given:
        String name = 'Nirav'
        BigDecimal grade = 100
        Long id = 1L
        controller.studentService = Stub(StudentService) {
            save(_, _) >> new Student(name: name, grade: grade, id: id)
        }

        when: 'json request is sent with domain conversion'
        request.method = 'POST'
        request.json = '{"name":"' + name + '","grade":' + grade + '}' // <1>
        controller.save()

        then: 'CREATED status code is set'
        response.status == 201 // <2>
    }
    // end::testSave3[]

    // tag::testEdit
    def '404 is returned if user tries to edit a null domain'() {
        when:
        controller.edit()

        then:
        response.status == 404
    }

    def "trying to edit an existing domain class populates the model"() {
        given:
        String name = 'Nirav'
        BigDecimal grade = 100
        Long id = 1
        controller.studentService = Stub(StudentService) {
            read(_) >> new Student(name: name, grade: grade, id: id)
        }
        when:
        params['id'] = id
        controller.edit()

        then:
        model.student
        model.student.name == name
        model.student.grade == grade
    }
    // end::testEdit[]

    // tag::testUpdate[]
    def "if a user tries to update a null domain class, he gets redirected to the student listing"() {
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'PUT'
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == '/student/index'
        flash.message != null
    }

    def 'try to update without supplying name and grade(both required) redirect to edit page again'() {
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'PUT'
        params.id = 1L
        controller.update()

        then:
        response.status == 200
        view == 'edit'
    }

    def "successful update redirects to the updated student and displays a message"() {
        String name = 'Nirav'
        BigDecimal grade = 100
        Long id = 1
        controller.studentService = Stub(StudentService) {
            update(_, _) >> new Student(name: name, grade: grade, id: id)
        }

        when:
        request.contentType = FORM_CONTENT_TYPE
        params.id = id
        params.grade = grade
        params.name = name
        request.method = 'PUT'
        controller.update()

        then: 'A redirect is issued to the show action'
        response.redirectedUrl.startsWith('/student/show')

        and: 'A message indicating the student has been updated is present'
        flash.message
    }
    // end::testUpdate[]

    // tag::testDelete[]
    def "trying to delete a null domain class, redirects the user to the students' listing"() {
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'DELETE'
        controller.delete()

        then:
        response.redirectedUrl == '/student/index'
        flash.message != null
    }

    void 'Test that the delete action deletes an instance if it exists'() {
        given:
        controller.studentService = Stub(StudentService) {
            delete(_) >> new Student()
        }

        when: 'A domain instance is created'
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'DELETE'
        params.id = 1L
        controller.delete()

        then: "user gets redirected to the students' listing"
        response.redirectedUrl == '/student/index'

        and: 'A mesage is populated indicating the successful deletion'
        flash.message
    }
    // end::testDelete[]

    // tag::testCalculateAvgGrade[]
    void 'Test controller correctly calls service method'() {
        given:
        controller.studentService = Mock(StudentService)

        when: 'controller is invoked to calculate grades'
        controller.calculateAvgGrade()

        then: 'verify the service was called and response received'
        response.text == 'Avg Grade is 100'
        1 * controller.studentService.calculateAvgGrade() >> 100
    }
    // end::testCalculateAvgGrade[]
// tag::specEnding[]
}
// end::specEnding[]

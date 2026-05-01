// tag::ClassroomGradesNotificationServiceSpecPackageImport[]
package grails.mock.basics

// end::ClassroomGradesNotificationServiceSpecPackageImport[]

// tag::ClassroomGradesNotificationServiceSpecImport[]
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification

// end::ClassroomGradesNotificationServiceSpecImport[]

// tag::ClassroomGradesNotificationServiceSpecClassDeclaration[]
@SuppressWarnings('MethodName')
class ClassroomGradesNotificationServiceSpec extends Specification
        implements DataTest, ServiceUnitTest<ClassroomGradesNotificationService> {
// end::ClassroomGradesNotificationServiceSpecClassDeclaration[]

    // tag::ClassroomGradesNotificationServiceSpecShared[]
    @Shared Classroom classroom
    // end::ClassroomGradesNotificationServiceSpecShared[]

    // tag::ClassroomGradesNotificationServiceSpecSetup[]
    def setupSpec() { // <1>
        mockDomain Student
        mockDomain Classroom
    }

    def setup() {
        classroom = new Classroom(teacher: 'Smith')
        [
                [name: 'Nirav', grade: 91],
                [name: 'Sergio', grade: 95],
                [name: 'Jeff', grade: 93],
        ].each {
            classroom.addToStudents(new Student(name: it.name, grade: it.grade))
        }
    }
    // end::ClassroomGradesNotificationServiceSpecSetup[]

    // tag::testEmailMock[]
    void 'test email students with mock collaborator'() {
        given: 'students are part of a classroom'
        def mockService = Mock(EmailService) // <2>
        service.emailService = mockService // <3>

        when: 'service is called to email students'
        int emailCount = service.emailClassroomStudents(classroom) // <4>

        then:
        1 * mockService.sendEmail([to: 'Sergio', from: 'Smith', note: 'Grade is 95']) >> 1 // <5>
        1 * mockService.sendEmail([to: 'Nirav', from: 'Smith', note: 'Grade is 91']) >> 1
        1 * mockService.sendEmail([to: 'Jeff', from: 'Smith', note: 'Grade is 93']) >> 1
        emailCount == 3
    }
    // end::testEmailMock[]

// tag::ClassroomGradesNotificationServiceCloseBrace[]
}
// end::ClassroomGradesNotificationServiceCloseBrace[]


package demo


import groovy.transform.CompileStatic
import org.springframework.context.MessageSource

@CompileStatic
class StudentController {

    static final int LARGE_NUMBER = 20000
    static scaffold = Student
    MessageSource messageSource

    StudentService studentService
    StudentDataService studentDataService

    def insert() {
        studentService.insertStudents(LARGE_NUMBER)
        render studentCountMessage()
    }

    def delete() {
        studentService.deleteStudentsWithGradleLessThanA()
        render studentCountMessage()
    }

    def print() {
        render studentService.htmlUnorderedListOfStudents()
    }

    def import25kStudents() {
        studentService.saveExcelStudents("src/resources/studentImport-25krows.xlsx")
        render studentCountMessage()
    }

    def import75kStudents() {
        studentService.saveExcelStudents("src/resources/studentImport-75krows.xlsx")
        render studentCountMessage()
    }

    protected String studentCountMessage() {
        int count = studentDataService.count()
        String defaultMsg = "Student Count: ${count}"
        messageSource.getMessage('student.count', [count] as Object[], defaultMsg, request.locale)
    }
}

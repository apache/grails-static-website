// tag::controllerPackageImport[]
package demo
// end::controllerPackageImport[]

// tag::controllerImports[]
// end::controllerImports[]

// tag::classDeclaration[]
class StudentController {
// end::classDeclaration[]

    // tag::allowedMethods[]
    static allowedMethods = [index: 'GET']
    // end::allowedMethods[]

    // tag::injectedStudentService[]
    def studentService
    // end::injectedStudentService[]

    // tag::indexAction[]
    def index() {
        [studentList : studentService.list(),
         studentCount: studentService.count()]
    }
    // end::indexAction[]

// tag::close[]
}
// end::close[]

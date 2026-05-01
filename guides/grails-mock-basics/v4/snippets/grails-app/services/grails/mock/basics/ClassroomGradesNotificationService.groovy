package grails.mock.basics

import groovy.transform.CompileStatic

@CompileStatic
class ClassroomGradesNotificationService {

    EmailService emailService

    int emailClassroomStudents(Classroom classroom) {
        int emailCount = 0
        for ( Student student in classroom.students ) {
            def email = emailFromTeacherToStudent(classroom.teacher, student)
            emailCount += emailService.sendEmail(email)
        }
        emailCount
    }

    Map emailFromTeacherToStudent(String teacher, Student student) {
        [
                to: "${student.name}",
                from: "${teacher}",
                note: "Grade is ${student.grade}",
        ]
    }
}

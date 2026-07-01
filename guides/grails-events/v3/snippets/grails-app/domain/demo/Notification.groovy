package demo

class Notification {
    String email
    String subject

    static constraints = {
        subject nullable: false
        email nullable: false, email: true
    }
}
package demo

class User {
    String firstName
    String lastName
    String email

    static constraints = {
        firstName nullable: false
        lastName nullable: false
        email nullable: false, email: true, unique: true
    }
}
package example.grails

class Book {
    String isbn
    String name

    static constraints = {
        isbn unique: true, blank: false, nullable: false
        name blank: false, nullable: false
    }
}
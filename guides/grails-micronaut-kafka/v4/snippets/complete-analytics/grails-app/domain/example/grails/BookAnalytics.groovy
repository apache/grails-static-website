package example.grails

class BookAnalytics {
    String isbn
    Long count

    static constraints = {
        isbn unique: true, blank: false, nullable: false
        count blank: false, nullable: false
    }
}

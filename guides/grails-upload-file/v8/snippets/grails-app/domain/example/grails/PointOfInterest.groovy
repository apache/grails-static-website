package example.grails

class PointOfInterest {

    String name
    String featuredImageUrl // <1>

    static constraints = {
        name nullable: false, blank: false
        featuredImageUrl nullable: true
    }
}

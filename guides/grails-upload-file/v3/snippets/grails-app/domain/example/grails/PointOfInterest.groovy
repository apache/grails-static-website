package example.grails

class PointOfInterest {
    String name
    String featuredImageUrl // <1>

    static constraints = {
        featuredImageUrl nullable: true
    }
}

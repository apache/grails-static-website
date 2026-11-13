package example.grails

class Restaurant {

    String name
    byte[] featuredImageBytes // <1>
    String featuredImageContentType // <2>

    static constraints = {
        name nullable: false, blank: false
        featuredImageBytes nullable: true, maxSize: 25 * 1024 * 1024 // <3>
        featuredImageContentType nullable: true
    }
}

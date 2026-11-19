package example.grails

class Hotel {

    String name
    String featuredImageKey // <1>
    String featuredImageUrl // <2>

    static constraints = {
        name nullable: false, blank: false
        featuredImageKey nullable: true
        featuredImageUrl nullable: true
    }
}

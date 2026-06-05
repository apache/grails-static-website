package example.grails

class Hotel {

    String name
    String featuredImageUrl // <1>
    String featuredImageKey // <2>

    static constraints = {
        featuredImageUrl nullable: true
        featuredImageKey nullable: true
    }
}

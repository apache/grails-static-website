package example.grails

class Restaurant {
    String name
    byte[] featuredImageBytes // <1>
    String featuredImageContentType // <2>

    static constraints = {
        featuredImageBytes nullable: true
        featuredImageContentType nullable: true
    }

    static mapping = {
        featuredImageBytes column: 'featured_image_bytes', sqlType: 'longblob' // <3>
    }
}

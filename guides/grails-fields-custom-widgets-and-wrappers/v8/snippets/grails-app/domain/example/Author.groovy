package example

class Author {

    String name
    String email
    String bio
    String website

    static hasOne = [contactInfo: ContactInfo]
    static hasMany = [books: Book]

    static constraints = {
        name blank: false, maxSize: 200
        email email: true, blank: false
        bio blank: false, maxSize: 4000, widget: 'textarea'
        website url: true, nullable: true
        contactInfo nullable: true
    }

    static mapping = {
        sort name: 'asc'
    }

    String toString() { name }
}

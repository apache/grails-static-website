package example

class Author {

    String name
    String email
    String bio
    String website

    static hasOne = [contactInfo: ContactInfo]
    static hasMany = [books: Book]

    static constraints = {
        name nullable: false, blank: false, maxSize: 200
        email nullable: false, email: true, blank: false
        bio nullable: false, blank: false, maxSize: 4000, widget: 'textarea'
        website url: true, nullable: true
        contactInfo nullable: true
    }

    def beforeValidate() {
        // The optional contact fieldset is always rendered. A blank submission
        // binds a new ContactInfo, which would fail its own required fields.
        if (contactInfo && !contactInfo.id && !contactInfo.phone && !contactInfo.mailingAddress) {
            contactInfo = null
        }
    }

    static mapping = {
        sort name: 'asc'
    }

    String toString() { name }
}

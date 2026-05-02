package example.library

class ContactInfo {

    String phone
    String mailingAddress

    static belongsTo = [author: Author]

    static constraints = {
        phone blank: false, maxSize: 32
        mailingAddress blank: false, maxSize: 500, widget: 'textarea'
    }

    String toString() { "Contact for ${author?.name}" }
}

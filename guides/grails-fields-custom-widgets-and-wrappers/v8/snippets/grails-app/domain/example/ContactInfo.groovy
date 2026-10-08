package example

class ContactInfo {

    String phone
    String mailingAddress

    static belongsTo = [author: Author]

    static constraints = {
        author nullable: false
        phone nullable: false, blank: false, maxSize: 32
        mailingAddress nullable: false, blank: false, maxSize: 500, widget: 'textarea'
    }

    String toString() { "Contact for ${author?.name}" }
}

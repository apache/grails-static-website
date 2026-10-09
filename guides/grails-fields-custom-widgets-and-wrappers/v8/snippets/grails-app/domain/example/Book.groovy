package example

class Book {

    String title
    String isbn
    String genre
    String description
    Date publishedDate
    BigDecimal priceUSD
    Boolean inStock = true

    static belongsTo = [author: Author]
    static hasMany = [tags: Tag]

    static constraints = {
        title nullable: false, blank: false, maxSize: 255
        isbn nullable: false, blank: false, matches: /^(?:\d{10}|\d{13}|\d{3}-\d-\d{2}-\d{6}-\d)$/
        author nullable: false
        genre nullable: false, inList: ['Fiction', 'Non-Fiction', 'Biography', 'Science', 'History', 'Poetry']
        description nullable: false, blank: false, maxSize: 2000, widget: 'textarea'
        publishedDate nullable: false
        priceUSD nullable: false, min: 0.00G, scale: 2
        inStock nullable: false
    }

    static mapping = {
        sort title: 'asc'
    }

    String toString() { title }
}

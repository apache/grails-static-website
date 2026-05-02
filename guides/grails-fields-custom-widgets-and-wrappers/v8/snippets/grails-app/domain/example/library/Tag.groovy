package example.library

class Tag {

    String name

    static hasMany = [books: Book]
    static belongsTo = Book

    static constraints = {
        name blank: false, maxSize: 60, unique: true
    }

    static mapping = {
        sort name: 'asc'
    }

    String toString() { name }
}

package demo

class Book {
    String title

    static hasMany = [keywords: Keyword]

    static mapping = {
        datasource 'books' // <1>
    }
}
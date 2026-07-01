package demo

class Movie {
    String title
    static hasMany = [keywords: Keyword]
}
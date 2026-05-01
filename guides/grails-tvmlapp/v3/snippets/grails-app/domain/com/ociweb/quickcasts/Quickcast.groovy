package com.ociweb.quickcasts

class Quickcast {
    String title
    String subtitle    
    String description
    int durationMinutes
    int durationSeconds
    int releaseYear
    String heroImg
    String videoUrl
    static hasMany = [authors: String]

    static constraints = {
        description nullable: true
    }

    static mapping = {
        description type: 'text'
    }
}

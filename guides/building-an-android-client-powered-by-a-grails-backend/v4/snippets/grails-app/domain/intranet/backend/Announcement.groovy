package intranet.backend

class Announcement {

    String title
    String body
    
    static constraints = {
        title size: 0..255
        body nullable: true
    }

    static mapping = {
        body type: 'text'  // <1>
    }
}

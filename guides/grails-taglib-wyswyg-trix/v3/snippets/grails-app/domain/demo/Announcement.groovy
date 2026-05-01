package demo

class Announcement {

    String title
    String message

    static constraints = {
        title maxSize: 255
    }

    static mapping = {
        message type: 'text'
    }
}

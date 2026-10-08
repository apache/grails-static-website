package example

class Make {

    String name

    static constraints = {
        name nullable: false, blank: false, maxSize: 255
    }

    String toString() {
        name
    }
}

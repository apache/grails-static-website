package example

class Make {

    String name

    static constraints = {
        name blank: false, maxSize: 255
    }

    String toString() {
        name
    }
}

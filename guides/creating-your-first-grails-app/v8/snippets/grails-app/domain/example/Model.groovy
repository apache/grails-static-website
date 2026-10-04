package example

class Model {

    String name

    static belongsTo = [make: Make]

    static constraints = {
        name blank: false, maxSize: 255
    }

    String toString() {
        name
    }
}

package example

class Model {

    String name

    static belongsTo = [make: Make]

    static constraints = {
        name nullable: false, blank: false, maxSize: 255
        make nullable: false
    }

    String toString() {
        name
    }
}

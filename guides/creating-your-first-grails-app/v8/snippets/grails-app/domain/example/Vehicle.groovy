package example

class Vehicle {

    String name
    Integer year
    Make make
    Model model

    static constraints = {
        name blank: false, maxSize: 255
        year min: 1900
    }

    String toString() {
        name
    }
}

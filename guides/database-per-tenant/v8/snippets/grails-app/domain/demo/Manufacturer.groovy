package demo

class Manufacturer {
    String name

    static constraints = {
        name nullable: false, blank: false
    }
}

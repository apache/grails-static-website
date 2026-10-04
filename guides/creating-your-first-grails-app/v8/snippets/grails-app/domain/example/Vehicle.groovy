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

    // H2 treats YEAR as a reserved word; quote/rename the physical column.
    static mapping = {
        year column: 'vehicle_year'
    }

    String toString() {
        name
    }
}

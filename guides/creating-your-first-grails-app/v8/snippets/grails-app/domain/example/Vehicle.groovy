package example

class Vehicle {

    String name
    Integer year
    Make make
    Model model

    static constraints = {
        name nullable: false, blank: false, maxSize: 255
        year nullable: false, min: 1900
        make nullable: false
        model nullable: false
    }

    // H2 treats YEAR as a reserved word; quote/rename the physical column.
    static mapping = {
        year column: 'vehicle_year'
    }

    String toString() {
        name
    }
}

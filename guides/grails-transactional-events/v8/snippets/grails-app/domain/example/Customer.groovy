package example

import grails.persistence.Entity

@Entity
class Customer {

    String name
    String email
    BigDecimal lifetimeValue = BigDecimal.ZERO

    static constraints = {
        name          blank: false, maxSize: 255
        email         email: true, unique: true, maxSize: 255
        lifetimeValue min: BigDecimal.ZERO
    }

    static mapping = {
        lifetimeValue scale: 2, precision: 19
    }

    String toString() { name }
}

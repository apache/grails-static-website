package example

import grails.persistence.Entity

@Entity
class Order {

    Customer customer
    BigDecimal total
    Date dateCreated

    static belongsTo = [customer: Customer]

    static constraints = {
        total min: new BigDecimal('0.01')
    }

    static mapping = {
        // 'order' is reserved in most SQL dialects
        table 'orders'
        total scale: 2, precision: 19
    }
}

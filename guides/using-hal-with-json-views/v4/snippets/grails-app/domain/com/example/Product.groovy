package com.example

class Product {

    String name
    String inventoryId

    BigDecimal price

    static belongsTo = [ category : Category ]
}

package demo

class Game {
    String name
    Integer minPlayers
    Integer maxPlayers
    Integer averageDuration     // in minutes
    BigDecimal rating           // from 0.0 to 10.0
    Boolean family = false
    Boolean party = false
    Boolean thematic = false
    Boolean strategy = false

    static hasMany = [categories: Category, mechanics: Mechanic]

    static constraints = {
        name blank: false, unique: true     // names are unique, at least, for this demo
        minPlayers min: 1
        maxPlayers validator: { val, obj -> val >= obj.minPlayers }
        averageDuration min: 1
        rating min: 0.0, max: 10.0
    }

    // tag::gameNamedQueries[]
    static namedQueries = {
        gamesWithMechanic { aMechanic ->
            mechanics {
                eq 'id', aMechanic.id
            }
        }
    }
    // end::gameNamedQueries[]
}
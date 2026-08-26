package demo

class Player {
    String name
    String game
    String region
    int wins = 0
    int losses = 0

    static constraints = {
        name blank: false, unique: true
        game blank: false
        region nullable: true
        wins min: 0
        losses min: 0
    }
}

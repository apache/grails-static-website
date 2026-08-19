package example.grails

class Delivery {
    String reference
    String status = 'PENDING'
    Integer progress = 0
    Date completedAt

    static constraints = {
        reference nullable: false, blank: false, unique: true
        status blank: false
        completedAt nullable: true
    }
}

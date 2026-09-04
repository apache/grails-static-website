package example

import grails.gorm.transactions.ReadOnly

class HomeController {

    @ReadOnly
    def index() {
        respond([
            name: session.name ?: 'User',
            vehicleList: Vehicle.list(),
            vehicleTotal: Vehicle.count()
        ])
    }

    def updateName(String name) {
        session.name = name
        flash.message = 'Name has been updated'
        redirect action: 'index'
    }
}

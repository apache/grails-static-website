package example

import grails.gorm.transactions.Transactional

@Transactional
class ValueEstimateService {

    def getEstimate(Vehicle vehicle) {
        // Placeholder — real apps would call a valuation API here
        Math.round(vehicle.name.size() + vehicle.model.name.size() * vehicle.year) * 2
    }
}

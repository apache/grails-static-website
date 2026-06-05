package org.grails.guides

import grails.gorm.transactions.Transactional

@Transactional
class ValueEstimateService {

    def getEstimate(Vehicle vehicle) {
        log.info 'Estimating vehicle value...'

        //TODO: Call third-party valuation API
        Math.round (vehicle.name.size() + vehicle.model.name.size() * vehicle.year) * 2
    }
}

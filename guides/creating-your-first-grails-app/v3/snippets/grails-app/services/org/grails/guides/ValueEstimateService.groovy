package org.grails.guides

import grails.transaction.Transactional

@Transactional
class ValueEstimateService {

    def getEstimate(Vehicle vehicle) {
        log.info 'Estimating vehicle value...'

        //TODO: Call third-party valuation API
        Math.round (vehicle.name.size() + vehicle.model.name.size() * vehicle.year) * 2
    }
}

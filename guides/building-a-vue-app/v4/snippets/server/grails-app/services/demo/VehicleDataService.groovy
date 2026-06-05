package demo

import grails.gorm.services.Service

@Service(Vehicle)
interface VehicleDataService {
    Vehicle save(String name, Driver driver, Make make, Model model)
}
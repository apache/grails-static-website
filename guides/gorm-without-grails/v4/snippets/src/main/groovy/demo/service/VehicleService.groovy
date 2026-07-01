package demo.service

import demo.domain.Manufacturer
import demo.domain.Vehicle
import grails.gorm.services.Where
import groovy.transform.CompileStatic
import org.springframework.stereotype.Service

@CompileStatic
@grails.gorm.services.Service(Vehicle)
@Service
interface VehicleService {

    @Where({ manufacturer.name == manufacturerName })
    List<Vehicle> findAllByManufacturer(String manufacturerName)

}

package example

import grails.gorm.services.Service

@Service(Manufacturer)
interface ManufacturerService {

    int count()

    Manufacturer save(String name)

    List<Manufacturer> findAll()

    Manufacturer findByName(String name)

}
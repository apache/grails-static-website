package demo

import grails.gorm.services.Service

@Service(Make)
interface MakeDataService {
    Make save(String name)
}
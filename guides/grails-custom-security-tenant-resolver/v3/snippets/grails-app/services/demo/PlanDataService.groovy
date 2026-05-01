package demo

import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.services.Service
import groovy.transform.CompileStatic

@CompileStatic
@Service(Plan) // <1>
@CurrentTenant // <2>
interface PlanDataService {
    List<Plan> findAll()
    Plan save(String title)
    void deleteByTitle(String title)
}

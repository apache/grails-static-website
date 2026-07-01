package demo

import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.services.Service

@Service(Plan) // <1>
@CurrentTenant // <2>
interface PlanDataService {
    List<Plan> findAll()
    Plan save(String title)
    void deleteByTitle(String title)
}

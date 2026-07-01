package example.grails

import grails.gorm.services.Service

@Service(RecordLabel)
interface RecordLabelService {
    RecordLabel save(String name)
}
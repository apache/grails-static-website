package demo

import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileStatic

@CompileStatic
class ModelService {

    @ReadOnly
    List<Model> listAll() {
        Model.where { }.list()
    }
}

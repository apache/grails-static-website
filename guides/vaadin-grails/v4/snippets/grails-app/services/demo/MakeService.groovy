package demo

import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileStatic

@CompileStatic
class MakeService {

    @ReadOnly
    List<Make> listAll() {
        Make.where { }.list()
    }
}

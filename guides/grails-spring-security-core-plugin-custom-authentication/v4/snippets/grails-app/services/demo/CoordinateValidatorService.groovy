package demo

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CoordinateValidatorService implements CoordinateValidator {

    @ReadOnly
    @Override
    boolean isValidValueForPositionAndUserName(String v, String p, String name) {
        SecurityCoordinate.where {
            position == p && value == v && user.username == name
        }.count() as boolean
    }
}
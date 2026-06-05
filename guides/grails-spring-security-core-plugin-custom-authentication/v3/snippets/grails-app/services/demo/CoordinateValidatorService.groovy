package demo

import grails.transaction.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CoordinateValidatorService implements CoordinateValidator {

    @Transactional(readOnly = true)
    @Override
    boolean isValidValueForPositionAndUserName(String v, String p, String name) {
        SecurityCoordinate.where {
            position == p && value == v && user.username == name
        }.count() as boolean
    }
}
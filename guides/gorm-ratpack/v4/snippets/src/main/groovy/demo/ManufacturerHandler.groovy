package demo

import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileStatic
import ratpack.exec.Blocking
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import static ratpack.jackson.Jackson.json

@CompileStatic
class ManufacturerHandler extends GroovyHandler {
    @Override
    protected void handle(GroovyContext context) {
        String manufacturerName = context.pathTokens.id
        Blocking.get {
            manufacturerName ? findAllVehicleNameByManufacturerName(manufacturerName) : findAllManufacturerName()
        } then { names ->
            context.render(json(names))
        }
    }

    @ReadOnly
    List<String> findAllVehicleNameByManufacturerName(String manufacturerName) {
        Vehicle.where { manufacturer.name == manufacturerName }.projections {
            property('name')
        }.list() as List<String>
    }
    @ReadOnly
    List<String> findAllManufacturerName() {
        Manufacturer.where {}.projections {
            property('name')
        }.list() as List<String>
    }
}

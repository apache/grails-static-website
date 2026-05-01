package org.grails.guides.scaffolding

import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.CREATED
import org.grails.guides.Vehicle
import grails.gorm.transactions.Transactional
import grails.gorm.transactions.ReadOnly

@SuppressWarnings(['LineLength'])
@ReadOnly // <1>
class VehicleController {

    static namespace = 'scaffolding'

    static allowedMethods = [save: 'POST', update: 'PUT', delete: 'DELETE']

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100) // <2>
        respond Vehicle.list(params), model:[vehicleCount: Vehicle.count()] // <3>
    }

    def show(Vehicle vehicle) {
        respond vehicle //<3>
    }

    @SuppressWarnings(['FactoryMethodName', 'GrailsMassAssignment'])
    def create() {
        respond new Vehicle(params) // <3>
    }

    @Transactional //<1>
    def save(Vehicle vehicle) {
        if (vehicle == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (vehicle.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond vehicle.errors, view:'create'  // <3>
            return
        }

        vehicle.save flush:true

        request.withFormat {  //<4>
            form multipartForm {
                //<5>
                flash.message = message(code: 'default.created.message', args: [message(code: 'vehicle.label', default: 'Vehicle'), vehicle.id])
                redirect vehicle
            }
            '*' { respond vehicle, [status: CREATED] } // <3>
        }
    }

    def edit(Vehicle vehicle) {
        respond vehicle //<3>
    }

    @Transactional //<1>
    def update(Vehicle vehicle) {
        if (vehicle == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (vehicle.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond vehicle.errors, view:'edit' //<3>
            return
        }

        vehicle.save flush:true

        request.withFormat {
            form multipartForm {
                //<5>
                flash.message = message(code: 'default.updated.message', args: [message(code: 'vehicle.label', default: 'Vehicle'), vehicle.id])
                redirect vehicle // <6>
            }
            '*' { respond vehicle, [status: OK] } // <3>
        }
    }

    @Transactional //<1>
    def delete(Vehicle vehicle) {

        if (vehicle == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        vehicle.delete flush:true

        request.withFormat {
            form multipartForm {
                //<5>
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'vehicle.label', default: 'Vehicle'), vehicle.id])
                redirect action: 'index', method: 'GET' // <6>
            }
            '*' { render status: NO_CONTENT } // <7>
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                //<5>
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'vehicle.label', default: 'Vehicle'), params.id])
                redirect action: 'index', method: 'GET' //<6>
            }
            '*' { render status: NOT_FOUND }  // <7>
        }
    }
}

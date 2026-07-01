package example

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

// Default for actions without their own @Secured; rejectIfNoRule denies anything that slips through unannotated.
@Secured(['ROLE_USER'])
class ResourceController {

    static responseFormats = ['json']
    static allowedMethods = [index: 'GET', show: 'GET', save: 'POST', update: 'PUT', delete: 'DELETE']

    @Secured(['permitAll'])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Resource.list(params), model: [resourceCount: Resource.count() as Long]
    }

    @Secured(['permitAll'])
    def show(Long id) {
        respond Resource.get(id)
    }

    @Secured(['isAuthenticated()'])
    @Transactional
    def save() {
        def resource = new Resource(request.JSON as Map)
        if (!resource.validate()) {
            respond resource.errors, status: 422
            return
        }
        resource.save(failOnError: true, flush: true)
        respond resource, status: 201
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def update(Long id) {
        def resource = Resource.get(id)
        if (!resource) {
            render status: 404
            return
        }
        resource.properties = request.JSON
        if (!resource.validate()) {
            respond resource.errors, status: 422
            return
        }
        resource.save(failOnError: true, flush: true)
        respond resource
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def delete(Long id) {
        def resource = Resource.get(id)
        if (!resource) {
            render status: 404
            return
        }
        resource.delete(flush: true)
        render status: 204
    }
}

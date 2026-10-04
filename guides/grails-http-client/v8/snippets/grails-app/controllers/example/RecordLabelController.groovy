package example

import grails.gorm.transactions.Transactional

class RecordLabelController {

    static responseFormats = ['json']
    static allowedMethods = [index: 'GET', show: 'GET', save: 'POST', update: 'PUT', delete: 'DELETE']

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond RecordLabel.list(params), model: [recordLabelCount: RecordLabel.count()]
    }

    def show(Long id) {
        respond RecordLabel.get(id)
    }

    @Transactional
    def save() {
        def recordLabel = new RecordLabel(request.JSON as Map)
        if (!recordLabel.validate()) {
            respond recordLabel.errors, status: 422
            return
        }
        recordLabel.save(failOnError: true, flush: true)
        respond recordLabel, status: 201
    }

    @Transactional
    def update(Long id) {
        def recordLabel = RecordLabel.get(id)
        if (!recordLabel) {
            render status: 404
            return
        }
        recordLabel.properties = request.JSON
        if (!recordLabel.validate()) {
            respond recordLabel.errors, status: 422
            return
        }
        recordLabel.save(failOnError: true, flush: true)
        respond recordLabel
    }

    @Transactional
    def delete(Long id) {
        def recordLabel = RecordLabel.get(id)
        if (!recordLabel) {
            render status: 404
            return
        }
        recordLabel.delete(flush: true)
        render status: 204
    }
}
